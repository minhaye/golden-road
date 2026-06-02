package goldenroad.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import goldenroad.util.AssetLoader;

public final class SoundEffectPlayer {
    private final Map<String, SoundData> cache = new HashMap<>();
    private final Set<Clip> activeClips = new HashSet<>();
    private final Map<String, Clip> loopingClips = new HashMap<>();
    private int volume;

    public SoundEffectPlayer(int volume) {
        setVolume(volume);
    }

    public synchronized void preload(String... resourcePaths) {
        if (resourcePaths == null) {
            return;
        }

        for (String resourcePath : resourcePaths) {
            try {
                load(resourcePath);
            } catch (IOException | UnsupportedAudioFileException e) {
                System.out.println("Loi khi load resource audio: " + resourcePath);
                e.printStackTrace();
            }
        }
    }

    public synchronized void play(String resourcePath) {
        try {
            Clip clip = createClip(resourcePath);
            if (clip != null) {
                clip.start();
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException | RuntimeException e) {
            System.out.println("Loi khi phat resource audio: " + resourcePath);
            e.printStackTrace();
        }
    }

    public synchronized void playLoop(String resourcePath) {
        Clip existingClip = loopingClips.get(resourcePath);
        if (existingClip != null && existingClip.isOpen()) {
            return;
        }

        try {
            Clip clip = createClip(resourcePath);
            if (clip != null) {
                loopingClips.put(resourcePath, clip);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
                clip.start();
            }
        } catch (IOException | UnsupportedAudioFileException | LineUnavailableException | RuntimeException e) {
            System.out.println("Loi khi phat resource audio: " + resourcePath);
            e.printStackTrace();
        }
    }

    public synchronized void stopLoop(String resourcePath) {
        Clip clip = loopingClips.remove(resourcePath);
        if (clip != null) {
            closeClip(clip);
        }
    }

    public synchronized void setVolume(int volume) {
        this.volume = Math.max(0, Math.min(100, volume));
        for (Clip clip : activeClips) {
            applyVolume(clip);
        }
    }

    private Clip createClip(String resourcePath)
        throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        SoundData soundData = load(resourcePath);
        if (soundData == null) {
            return null;
        }

        Clip clip = AudioSystem.getClip();
        try {
            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    closeClipIfNotLooping(clip);
                }
            });
            clip.open(soundData.format, soundData.data, 0, soundData.data.length);
            applyVolume(clip);
            activeClips.add(clip);
            return clip;
        } catch (LineUnavailableException | RuntimeException e) {
            clip.close();
            throw e;
        }
    }

    private SoundData load(String resourcePath) throws IOException, UnsupportedAudioFileException {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }

        SoundData cachedData = cache.get(resourcePath);
        if (cachedData != null) {
            return cachedData;
        }

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
                return null;
            }

            SoundData soundData = readPlaybackData(audioStream);
            cache.put(resourcePath, soundData);
            return soundData;
        }
    }

    private SoundData readPlaybackData(AudioInputStream audioStream) throws IOException {
        AudioFormat sourceFormat = audioStream.getFormat();
        AudioFormat playbackFormat = toPlaybackFormat(sourceFormat);

        if (!requiresConversion(sourceFormat)) {
            return new SoundData(sourceFormat, audioStream.readAllBytes());
        }

        if (!AudioSystem.isConversionSupported(playbackFormat, sourceFormat)) {
            return new SoundData(sourceFormat, audioStream.readAllBytes());
        }

        try (AudioInputStream playbackStream = AudioSystem.getAudioInputStream(playbackFormat, audioStream)) {
            return new SoundData(playbackStream.getFormat(), playbackStream.readAllBytes());
        }
    }

    private boolean requiresConversion(AudioFormat format) {
        return format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED
            || format.getSampleSizeInBits() != 16
            || format.isBigEndian();
    }

    private AudioFormat toPlaybackFormat(AudioFormat sourceFormat) {
        int channels = sourceFormat.getChannels();
        return new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            sourceFormat.getSampleRate(),
            16,
            channels,
            channels * 2,
            sourceFormat.getSampleRate(),
            false
        );
    }

    private synchronized void closeClipIfNotLooping(Clip clip) {
        if (!loopingClips.containsValue(clip)) {
            closeClip(clip);
        }
    }

    private synchronized void closeClip(Clip clip) {
        activeClips.remove(clip);
        loopingClips.values().removeIf(loopingClip -> loopingClip == clip);
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.close();
    }

    private void applyVolume(Clip clip) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }

        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float gain = volume == 0
            ? gainControl.getMinimum()
            : (float) (20.0 * Math.log10(volume / 100.0));
        gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), gain)));
    }

    private static final class SoundData {
        private final AudioFormat format;
        private final byte[] data;

        private SoundData(AudioFormat format, byte[] data) {
            this.format = format;
            this.data = data;
        }
    }
}
