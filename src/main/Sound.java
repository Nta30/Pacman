package main;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.net.URL;

public class Sound {
    Clip clip;
    Clip seClip; // separate clip for sound effects
    URL[] soundURL = new URL[30];
    FloatControl fc;
    int volumeScale = 3;
    float volume;

    public Sound() {
        // Reuse existing sound files where applicable
        soundURL[0] = getClass().getResource("/sound/BlueBoyAdventure.wav"); // background music
        soundURL[1] = getClass().getResource("/sound/coin.wav");             // dot eat
        soundURL[2] = getClass().getResource("/sound/powerup.wav");          // power pellet
        soundURL[3] = getClass().getResource("/sound/hitmonster.wav");       // eat ghost
        soundURL[4] = getClass().getResource("/sound/receivedamage.wav");    // pacman death
        soundURL[5] = getClass().getResource("/sound/fanfare.wav");          // level complete
        soundURL[6] = getClass().getResource("/sound/levelup.wav");          // extra life
        soundURL[7] = getClass().getResource("/sound/cursor.wav");           // menu cursor
        soundURL[8] = getClass().getResource("/sound/gameover.wav");         // game over
    }

    public void setFile(int i) {
        if (soundURL[i] == null) return;
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            clip = AudioSystem.getClip();
            clip.open(ais);
            fc = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            checkVolume();
        } catch (Exception e) {
            // Graceful: don't crash if sound file missing
        }
    }

    public void play() {
        if (clip != null) {
            clip.start();
        }
    }

    public void loop() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
        }
    }

    /**
     * Play a one-shot sound effect without affecting the main clip
     */
    public void playSE(int i) {
        if (i < 0 || i >= soundURL.length || soundURL[i] == null) return;
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
            Clip seClip = AudioSystem.getClip();
            seClip.open(ais);
            FloatControl sefc = (FloatControl) seClip.getControl(FloatControl.Type.MASTER_GAIN);
            // Set volume
            float vol;
            switch (volumeScale) {
                case 0: vol = -80f; break;
                case 1: vol = -20f; break;
                case 2: vol = -12f; break;
                case 3: vol = -5f; break;
                case 4: vol = 1f; break;
                case 5: vol = 6f; break;
                default: vol = -5f;
            }
            sefc.setValue(vol);
            seClip.start();
        } catch (Exception e) {
            // Graceful: ignore missing sounds
        }
    }

    public void checkVolume() {
        if (fc == null) return;
        switch (volumeScale) {
            case 0: volume = -80f; break;
            case 1: volume = -20f; break;
            case 2: volume = -12f; break;
            case 3: volume = -5f; break;
            case 4: volume = 1f; break;
            case 5: volume = 6f; break;
        }
        fc.setValue(volume);
    }
}
