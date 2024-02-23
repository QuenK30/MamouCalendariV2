package fr.qmn.mamoucalendari.ocr;


import fr.qmn.mamoucalendari.utils.TimeLib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InitOCR {
    private final String scriptPath = "src/main/java/fr/qmn/mamoucalendari/ocr/scripts/OCRCall.py";

    public String receiveImageForOCR(String imgPath) {
        TimeLib timeLib = new TimeLib();
        StringBuilder output = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath, imgPath);
            Process p = pb.start();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line;
            while ((line = stdInput.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }

            boolean isError = false;
            while ((line = stdError.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
                isError = true;
            }

            int exitCode = p.waitFor();
            System.out.println("Exited with error code : " + exitCode);
            System.out.println(output);
            if (exitCode == 0 && !isError) {
                String result = output.toString();
                int start = result.indexOf("<");
                int end = result.indexOf(">");
                return result.substring(start + 1, end);
            } else {
                return "Error in OCR Process: " + output;
            }

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Exception occurred in OCR process: " + e.getMessage(), e);
        }
    }
}
