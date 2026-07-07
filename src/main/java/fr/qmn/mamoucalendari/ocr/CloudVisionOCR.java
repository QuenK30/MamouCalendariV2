package fr.qmn.mamoucalendari.ocr;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageContext;
import com.google.protobuf.ByteString;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class CloudVisionOCR {

    public static String recognize(BufferedImage image) {
        if (System.getenv("GOOGLE_APPLICATION_CREDENTIALS") == null) {
            System.out.println("[CloudVisionOCR] GOOGLE_APPLICATION_CREDENTIALS non défini — OCR désactivé.");
            return "";
        }
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            ByteString imgBytes = ByteString.copyFrom(baos.toByteArray());

            Image img = Image.newBuilder().setContent(imgBytes).build();
            ImageContext ctx = ImageContext.newBuilder().addLanguageHints("fr").build();
            Feature feat = Feature.newBuilder()
                    .setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
            AnnotateImageRequest req = AnnotateImageRequest.newBuilder()
                    .setImage(img).setImageContext(ctx).addFeatures(feat).build();

            try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                BatchAnnotateImagesResponse response =
                        client.batchAnnotateImages(List.of(req));
                String text = response.getResponses(0)
                        .getFullTextAnnotation().getText().trim();
                System.out.println("[CloudVisionOCR] Résultat : \"" + text + "\"");
                return text;
            }
        } catch (Exception e) {
            System.out.println("[CloudVisionOCR] Erreur : " + e.getMessage());
            return "";
        }
    }
}
