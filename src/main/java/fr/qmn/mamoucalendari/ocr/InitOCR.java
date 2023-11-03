package fr.qmn.mamoucalendari.ocr;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;

import java.io.File;

public class InitOCR {

    public void receiveImageForOCR(String imgPath) {
        System.out.println("receiveImageForOCR: " + imgPath);
        String result = "";
        String tessPath = "src/main/resources/fr/qmn/mamoucalendari/data";
        ITesseract instance = new Tesseract();
        instance.setDatapath(tessPath);
        instance.setLanguage("fra");
        System.out.println("Tesseract");
        try {
            result = instance.doOCR(new File(imgPath));
            System.out.println(result);
            System.out.println("OCR done");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

}
