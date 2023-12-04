package fr.qmn.mamoucalendari.ocr;

import fr.qmn.mamoucalendari.bdd.SQLManager;
import fr.qmn.mamoucalendari.utils.TimeLib;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import java.io.File;
import java.io.IOException;

public class InitOCR {
    private SQLManager sqlManager = new SQLManager();
    private String scriptPath = "src/main/java/fr/qmn/mamoucalendari/ocr/OCROpenAI.py";

    public void receiveImageForOCR(String imgPath, String hours, String minutes, String date) {
        TimeLib timeLib = new TimeLib();
        try {
            ProcessBuilder pb = new ProcessBuilder("python", scriptPath, imgPath);
            Process p = pb.start();
            String result = pb.command().get(1);
            System.out.println(result);
            int exitCode = p.waitFor();
            System.out.println("Exited with error code : " + exitCode);
            if (exitCode == 0) {
                String convertDate = timeLib.convertDate(date);
                System.out.println("Date: " + convertDate);
                sqlManager.createTask(convertDate, Integer.parseInt(hours), Integer.parseInt(minutes), result);
                System.out.println("Successfully added task to database");
                System.out.println("Task: " + result + " at " + hours + ":" + minutes + " on " + convertDate);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // Python script for calling OCR openai


}
