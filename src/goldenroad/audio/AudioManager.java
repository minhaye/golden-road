package goldenroad.audio;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

public class AudioManager {
    private final Map<String, Clip> music = new HashMap<>();
    private final Map<String, Clip> sfx = new HashMap<>();

    private float musicVolume = 0.6f;
    private float sfxVolume = 0.8f;

    public void loadMusic(String name, String path) {
        Clip clip = loadClip(path);
        if (clip != null) {
            setClipVolume(clip, musicVolume);
            music.put(name, clip);
        }
    }

    public void loadSfx(String name, String path) {
        Clip clip = loadClip(path);
        if (clip != null) {
            setClipVolume(clip, sfxVolume);
            sfx.put(name, clip);
        }
    }

    public void playMusic(String name) {
        Clip clip = music.get(name);
        if (clip == null || clip.isRunning())
            return;

        clip.setFramePosition(0);
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    public void stopMusic(String name) {
        Clip clip = music.get(name);
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    public void playSfx(String name) {
        Clip clip = sfx.get(name);
        if (clip == null)
            return;

        if (clip.isRunning()) {
            clip.stop();
        }

        clip.setFramePosition(0);
        clip.start();
    }

    public void setMusicVolume(float volume) {
        musicVolume = clamp(volume);
        for (Clip clip : music.values()) {
            setClipVolume(clip, musicVolume);
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setSfxVolume(float volume) {
        sfxVolume = clamp(volume);
        for (Clip clip : sfx.values()) {
            setClipVolume(clip, sfxVolume);
        }
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    private Clip loadClip(String path) {
        try {
            InputStream raw = getClass().getResourceAsStream(path);
            if (raw == null) {
                System.out.println("Không tìm thấy audio: " + path);
                return null;
            }

            BufferedInputStream buffered = new BufferedInputStream(raw);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(buffered);

            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;

        } catch (Exception e) {
            System.out.println("Lỗi load audio: " + path);
            e.printStackTrace();
            return null;
        }
    }

    private void setClipVolume(Clip clip, float volume) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
            return;

        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float safeVolume = Math.max(0.0001f, clamp(volume));
        float db = (float) (20.0 * Math.log10(safeVolume));
        gain.setValue(Math.max(gain.getMinimum(), Math.min(db, gain.getMaximum())));
    }

    private float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
