package goldenroad.audio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundEffect {
    private final String resourcePath;
    private Clip clip;

    public SoundEffect(String resourcePath) {
        this.resourcePath = resourcePath;
        load();
    }

    public synchronized void play() {
        if (clip == null) {
            return;
        }

        if (clip.isRunning()) {
            clip.stop();
        }

        clip.setFramePosition(0);
        clip.start();
    }

    private void load() {
        try (InputStream rawStream = open(resourcePath)) {
            if (rawStream == null) {
                System.out.println("Khong tim thay audio: " + resourcePath);
                return;
            }

            try (BufferedInputStream bufferedStream = new BufferedInputStream(rawStream);
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedStream)) {
                Clip loadedClip = AudioSystem.getClip();
                loadedClip.open(audioStream);
                clip = loadedClip;
            }
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            System.out.println("Loi khi load audio: " + resourcePath);
            e.printStackTrace();
        }
    }

    private InputStream open(String path) throws IOException {
        String normalizedPath = normalize(path);

        InputStream classpathStream = SoundEffect.class.getResourceAsStream("/" + normalizedPath);
        if (classpathStream != null) {
            return classpathStream;
        }

        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            classpathStream = loader.getResourceAsStream(normalizedPath);
            if (classpathStream != null) {
                return classpathStream;
            }
        }

        Path sourcePath = Path.of("src").resolve(normalizedPath);
        if (Files.exists(sourcePath)) {
            return Files.newInputStream(sourcePath);
        }

        Path workingPath = Path.of(normalizedPath);
        if (Files.exists(workingPath)) {
            return Files.newInputStream(workingPath);
        }

        return null;
    }

    private String normalize(String path) {
        if (path == null) {
            return "";
        }

        String normalizedPath = path.replace('\\', '/');
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        return normalizedPath;
    }
}
