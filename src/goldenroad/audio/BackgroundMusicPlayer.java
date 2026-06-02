package goldenroad.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import goldenroad.util.AssetLoader;

public final class BackgroundMusicPlayer {
    private Clip clip;
    private String currentResourcePath;
    private int volume;

    public BackgroundMusicPlayer(int volume) {
        setVolume(volume);
    }

    public void playLoop(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            stop();
            return;
        }

        if (resourcePath.equals(currentResourcePath) && clip != null && clip.isOpen()) {
            applyVolume();
            if (!clip.isRunning()) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            }
            return;
        }

        stop();

        try (
            InputStream resourceStream = AssetLoader.open(resourcePath);
            BufferedInputStream bufferedStream = resourceStream == null
                ? null
                : new BufferedInputStream(resourceStream);
            AudioInputStream audioStream = bufferedStream == null
                ? null
                : AudioSystem.getAudioInputStream(bufferedStream)
        ) {
            if (audioStream == null) {
                System.out.println("Khong tim thay resource audio: " + resourcePath);
                return;
            }

            Clip newClip = AudioSystem.getClip();
            try {
                newClip.open(audioStream);
                clip = newClip;
                currentResourcePath = resourcePath;
                applyVolume();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            } catch (LineUnavailableException | RuntimeException e) {
                newClip.close();
                throw e;
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException | RuntimeException e) {
            System.out.println("Loi khi load resource audio: " + resourcePath);
            e.printStackTrace();
        }
    }

    public void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        applyVolume();
    }

    public void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
        }
        currentResourcePath = null;
    }

    private void applyVolume() {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float gain = volume == 0
            ? gainControl.getMinimum()
            : (float) (20.0 * Math.log10(volume / 100.0));
        gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), gain)));
    }
}
