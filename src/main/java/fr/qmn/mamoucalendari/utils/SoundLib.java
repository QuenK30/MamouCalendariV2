package fr.qmn.mamoucalendari.utils;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundLib {
    public void playSound(String soundPath) {
        try {
            File sound = new File(soundPath);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(sound);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void testSound() {
        playSound("src/main/resources/fr/qmn/mamoucalendari/sounds/TaskAdded.wav");
        System.out.println("Sound played");
    }
}
