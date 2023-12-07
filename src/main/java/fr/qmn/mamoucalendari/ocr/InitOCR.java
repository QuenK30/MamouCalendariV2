package fr.qmn.mamoucalendari.ocr;

import fr.qmn.mamoucalendari.bdd.SQLManager;
import fr.qmn.mamoucalendari.utils.TimeLib;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import java.io.File;
import java.io.IOException;

public class InitOCR {
    private String scriptPath = "src/main/java/fr/qmn/mamoucalendari/ocr/scripts/OCROpenAICall.py";

    public String receiveImageForOCR(String imgPath, String hours, String minutes, String date) {
        TimeLib timeLib = new TimeLib();
        try {
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath, imgPath);
            Process p = pb.start();
            String result = pb.command().get(1);
            System.out.println("result : "+result);
            int exitCode = p.waitFor();
            System.out.println("Exited with error code : " + exitCode);
            if (exitCode == 0) {
                return result;
            }else {
                return "error";
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
