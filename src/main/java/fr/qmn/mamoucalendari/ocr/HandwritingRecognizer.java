package fr.qmn.mamoucalendari.ocr;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OnnxValue;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Moteur de reconnaissance d'écriture manuscrite basé sur TrOCR (Xenova/trocr-small-handwritten).
 * Architecture : DeiT encoder + TrOCR decoder autorégressif, quantisé INT8.
 * Runtime : ONNX Runtime Java 1.27.0 (ARM64, Raspberry Pi 5).
 */
public class HandwritingRecognizer implements AutoCloseable {

    private static final String ENCODER_CLASSPATH   = "/fr/qmn/mamoucalendari/ocr/encoder.onnx";
    private static final String DECODER_CLASSPATH   = "/fr/qmn/mamoucalendari/ocr/decoder.onnx";
    private static final String TOKENIZER_CLASSPATH = "/fr/qmn/mamoucalendari/ocr/tokenizer.json";

    // Taille d'entrée fixe du ViT/DeiT — toujours 384×384
    private static final int IMG_SIZE        = 384;
    // Limite de génération (tokens) pour éviter une boucle infinie
    private static final int MAX_TOKENS      = 30;
    // Tokens spéciaux XLMRoberta : decoder_start = eos = 2
    private static final int START_TOKEN     = 2;
    private static final int EOS_TOKEN       = 2;

    private static final int WHITE_THRESHOLD = 240;
    private static final int CROP_PADDING    = 2;
    private static final boolean DEBUG_IMAGES = true;

    private final OrtEnvironment        env;
    private final OrtSession            encoderSession;
    private final OrtSession            decoderSession;
    private final Map<Integer, String>  vocab;   // token_id → token_string

    public HandwritingRecognizer() {
        OrtEnvironment       tempEnv     = null;
        OrtSession           tempEncoder = null;
        OrtSession           tempDecoder = null;
        Map<Integer, String> tempVocab   = null;

        try {
            tempEnv     = OrtEnvironment.getEnvironment();
            tempEncoder = tempEnv.createSession(
                    extractToTemp(ENCODER_CLASSPATH, "trocr_enc_").toString(),
                    new OrtSession.SessionOptions());
            System.out.println("[HandwritingRecognizer] Encoder chargé : " + ENCODER_CLASSPATH);

            tempDecoder = tempEnv.createSession(
                    extractToTemp(DECODER_CLASSPATH, "trocr_dec_").toString(),
                    new OrtSession.SessionOptions());
            System.out.println("[HandwritingRecognizer] Decoder chargé : " + DECODER_CLASSPATH);
            System.out.println("[HandwritingRecognizer] Decoder inputs  : " + tempDecoder.getInputNames());
            System.out.println("[HandwritingRecognizer] Decoder outputs : " + tempDecoder.getOutputNames());

            tempVocab = loadVocab();
            System.out.println("[HandwritingRecognizer] Vocab : " + tempVocab.size() + " tokens");

        } catch (IOException e) {
            System.out.println("[HandwritingRecognizer] Ressource introuvable : " + e.getMessage());
            System.out.println("[HandwritingRecognizer] L'OCR est désactivé.");
        } catch (OrtException e) {
            System.out.println("[HandwritingRecognizer] Erreur ONNX Runtime : " + e.getMessage());
            System.out.println("[HandwritingRecognizer] L'OCR est désactivé.");
        }

        this.env            = tempEnv;
        this.encoderSession = tempEncoder;
        this.decoderSession = tempDecoder;
        this.vocab          = tempVocab;
    }

    // -------------------------------------------------------------------------
    // Chargement des ressources
    // -------------------------------------------------------------------------

    private Path extractToTemp(String classpath, String prefix) throws IOException {
        try (InputStream stream = HandwritingRecognizer.class.getResourceAsStream(classpath)) {
            if (stream == null) throw new IOException("Absent du classpath : " + classpath);
            Path temp = Files.createTempFile(prefix, ".onnx");
            temp.toFile().deleteOnExit();
            Files.copy(stream, temp, StandardCopyOption.REPLACE_EXISTING);
            return temp;
        }
    }

    private Map<Integer, String> loadVocab() throws IOException {
        try (InputStream stream = HandwritingRecognizer.class.getResourceAsStream(TOKENIZER_CLASSPATH)) {
            if (stream == null) throw new IOException("Absent du classpath : " + TOKENIZER_CLASSPATH);
            String json;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line).append('\n');
                json = sb.toString();
            }
            // Le vocab est une liste : "vocab": [["token", score], ["token", score], ...]
            // L'ID de chaque token est son index dans la liste.
            int vocabKey = json.indexOf("\"vocab\":");
            if (vocabKey == -1) throw new IOException("Bloc 'vocab' introuvable dans tokenizer.json");
            String after = json.substring(vocabKey + "\"vocab\":".length());
            int arrayStart = after.indexOf('[');
            if (arrayStart == -1) throw new IOException("Tableau vocab introuvable");

            // Parser les paires ["token", score] avec une regex
            Map<Integer, String> map = new HashMap<>();
            Pattern p = Pattern.compile("\\[\"((?:[^\"\\\\]|\\\\.)*)\",");
            Matcher m = p.matcher(after);
            m.region(arrayStart, after.length());
            int id = 0;
            while (m.find()) {
                String token = m.group(1)
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                        .replace("\\/", "/")
                        .replace("\\n", "\n")
                        .replace("\\t", "\t");
                map.put(id++, token);
            }
            return map;
        }
    }

    // -------------------------------------------------------------------------
    // Point d'entrée public
    // -------------------------------------------------------------------------

    /**
     * Reconnaît le texte manuscrit dessiné sur le canvas.
     *
     * Pipeline TrOCR :
     *   1. Niveaux de gris → recadrage de l'écriture
     *   2. Resize 384×384 RGB → normalisation [-1, 1] → tenseur [1, 3, 384, 384]
     *   3. Encoder DeiT → last_hidden_state [1, 578, 384]
     *   4. Décodage autorégressif (greedy) jusqu'à EOS ou MAX_TOKENS
     *   5. Décodage tokens → texte via vocab
     *
     * @param image image issue du canvas (RGB ou niveaux de gris)
     * @return texte reconnu, ou "" si modèle non prêt / canvas vierge
     */
    public String recognize(BufferedImage image) {
        if (!isReady()) {
            System.out.println("[HandwritingRecognizer] Modèle non initialisé.");
            return "";
        }
        if (image == null || image.getWidth() == 0 || image.getHeight() == 0) return "";

        try {
            // 1. Prétraitement
            BufferedImage gray    = toGrayscale(image);
            saveDebug(gray, "01-original.png");
            BufferedImage cropped = cropMargins(gray);
            if (cropped == null) {
                System.out.println("[HandwritingRecognizer] Canvas vierge.");
                return "";
            }
            saveDebug(cropped, "02-cropped.png");
            float[] pixelValues = preprocessToTensor(cropped);

            // 2. Encoder
            float[] hiddenFlat;
            long[]  hiddenShape;
            try (OnnxTensor pixTensor = OnnxTensor.createTensor(
                         env, FloatBuffer.wrap(pixelValues), new long[]{1, 3, IMG_SIZE, IMG_SIZE});
                 OrtSession.Result encResult = encoderSession.run(Map.of("pixel_values", pixTensor))) {

                OnnxTensor encOut = getTensor(encResult, "last_hidden_state");
                long[] s = encOut.getInfo().getShape();
                System.out.println("[HandwritingRecognizer] Encoder sortie : " + Arrays.toString(s));
                int seqLen = (int) s[1], hidDim = (int) s[2];
                float[][][] hs = (float[][][]) encOut.getValue();
                hiddenFlat  = new float[seqLen * hidDim];
                hiddenShape = new long[]{1, seqLen, hidDim};
                for (int i = 0; i < seqLen; i++)
                    System.arraycopy(hs[0][i], 0, hiddenFlat, i * hidDim, hidDim);
            }

            // 3. Décodage autorégressif (greedy, sans KV cache)
            long[] ids = {START_TOKEN};
            for (int step = 0; step < MAX_TOKENS; step++) {
                int T = ids.length;

                try (OnnxTensor tIds   = OnnxTensor.createTensor(env, LongBuffer.wrap(ids),   new long[]{1, T});
                     OnnxTensor tHid   = OnnxTensor.createTensor(env, FloatBuffer.wrap(hiddenFlat), hiddenShape);
                     OrtSession.Result decResult = decoderSession.run(Map.of(
                             "input_ids",             tIds,
                             "encoder_hidden_states", tHid))) {

                    OnnxTensor logitTensor = getTensor(decResult, "logits");
                    float[][][] logits     = (float[][][]) logitTensor.getValue();
                    int nextToken = argmax(logits[0][T - 1]);
                    System.out.println("[HandwritingRecognizer] Step " + step
                            + " → " + nextToken + " (" + vocab.getOrDefault(nextToken, "?") + ")");
                    if (nextToken == EOS_TOKEN) break;
                    ids = Arrays.copyOf(ids, ids.length + 1);
                    ids[ids.length - 1] = nextToken;
                }
            }

            // 4. Tokens → texte
            String result = decodeTokens(ids);
            System.out.println("[HandwritingRecognizer] Résultat : \"" + result + "\"");
            return result;

        } catch (OrtException e) {
            System.out.println("[HandwritingRecognizer] Erreur d'inférence : " + e.getMessage());
        }
        return "";
    }

    private int argmax(float[] arr) {
        int best = 0;
        for (int i = 1; i < arr.length; i++) {
            if (arr[i] > arr[best]) best = i;
        }
        return best;
    }

    private String decodeTokens(long[] ids) {
        // ids[0] est le START_TOKEN — on l'ignore
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < ids.length; i++) {
            String token = vocab.getOrDefault((int) ids[i], "");
            // XLMRoberta SentencePiece : ▁ (U+2581) marque l'espace avant le token
            // Roberta BPE              : Ġ (U+0120) marque l'espace avant le token
            sb.append(token.replace('▁', ' ').replace('Ġ', ' '));
        }
        return sb.toString().trim();
    }

    private OnnxTensor getTensor(OrtSession.Result result, String name) throws OrtException {
        for (Map.Entry<String, OnnxValue> entry : result) {
            if (entry.getKey().equals(name) && entry.getValue() instanceof OnnxTensor t) return t;
        }
        StringBuilder available = new StringBuilder();
        for (Map.Entry<String, OnnxValue> entry : result) available.append(entry.getKey()).append(' ');
        throw new OrtException("Tenseur '" + name + "' absent. Disponibles : " + available);
    }

    // -------------------------------------------------------------------------
    // Prétraitement image
    // -------------------------------------------------------------------------

    private float[] preprocessToTensor(BufferedImage crop) {
        // Resize vers IMG_SIZE×IMG_SIZE (ratio non conservé)
        BufferedImage rgb = new BufferedImage(IMG_SIZE, IMG_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,     RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(crop, 0, 0, IMG_SIZE, IMG_SIZE, null);
        g.dispose();
        saveDebug(rgb, "03-trocr-input.png");

        // Layout [C, H, W] normalisé : (pixel/255 - 0.5) / 0.5
        float[] flat = new float[3 * IMG_SIZE * IMG_SIZE];
        int area = IMG_SIZE * IMG_SIZE;
        for (int y = 0; y < IMG_SIZE; y++) {
            for (int x = 0; x < IMG_SIZE; x++) {
                int px = rgb.getRGB(x, y);
                float r  = ((px >> 16) & 0xFF) / 255.0f;
                float gv = ((px >> 8)  & 0xFF) / 255.0f;
                float b  = ( px        & 0xFF) / 255.0f;
                int offset = y * IMG_SIZE + x;
                flat[offset]          = (r  - 0.5f) / 0.5f;
                flat[area + offset]   = (gv - 0.5f) / 0.5f;
                flat[2 * area + offset] = (b  - 0.5f) / 0.5f;
            }
        }
        return flat;
    }

    private void saveDebug(BufferedImage img, String filename) {
        if (!DEBUG_IMAGES) return;
        try {
            File dir = new File("debug");
            if (!dir.exists()) dir.mkdirs();
            ImageIO.write(img, "png", new File(dir, filename));
        } catch (IOException e) {
            System.out.println("[HandwritingRecognizer] debug/" + filename + " : " + e.getMessage());
        }
    }

    private BufferedImage toGrayscale(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_BYTE_GRAY) return src;
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return gray;
    }

    private BufferedImage cropMargins(BufferedImage gray) {
        int w = gray.getWidth(), h = gray.getHeight();
        Raster raster = gray.getRaster();
        int top = -1, bottom = -1, left = -1, right = -1;

        topSearch:
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                if (raster.getSample(x, y, 0) < WHITE_THRESHOLD) { top = y; break topSearch; }
        if (top == -1) return null;

        bottomSearch:
        for (int y = h - 1; y >= top; y--)
            for (int x = 0; x < w; x++)
                if (raster.getSample(x, y, 0) < WHITE_THRESHOLD) { bottom = y; break bottomSearch; }

        leftSearch:
        for (int x = 0; x < w; x++)
            for (int y = top; y <= bottom; y++)
                if (raster.getSample(x, y, 0) < WHITE_THRESHOLD) { left = x; break leftSearch; }

        rightSearch:
        for (int x = w - 1; x >= left; x--)
            for (int y = top; y <= bottom; y++)
                if (raster.getSample(x, y, 0) < WHITE_THRESHOLD) { right = x; break rightSearch; }

        left   = Math.max(0,     left   - CROP_PADDING);
        top    = Math.max(0,     top    - CROP_PADDING);
        right  = Math.min(w - 1, right  + CROP_PADDING);
        bottom = Math.min(h - 1, bottom + CROP_PADDING);

        return gray.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }

    // -------------------------------------------------------------------------
    // Cycle de vie
    // -------------------------------------------------------------------------

    @Override
    public void close() {
        try {
            if (encoderSession != null) encoderSession.close();
            if (decoderSession != null) decoderSession.close();
            if (env != null) env.close();
        } catch (OrtException e) {
            System.out.println("[HandwritingRecognizer] Erreur fermeture : " + e.getMessage());
        }
    }

    public boolean isReady() {
        return encoderSession != null && decoderSession != null && vocab != null;
    }
}
