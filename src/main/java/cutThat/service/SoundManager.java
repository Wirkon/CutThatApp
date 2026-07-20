package cutThat.service;


import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class SoundManager {

    public static void playNotification(){
        try{
        InputStream audioSrc = SoundManager.class.getClassLoader().getResourceAsStream("sounds/notif.wav");
            if(audioSrc != null){
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            try(AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }
            } else {
                Toolkit.getDefaultToolkit().beep();
            }
        }catch(Exception e){
            System.out.println("Не удалось воспроизвести звуку уведомления: "+ e.getMessage());
        }
    }
}
