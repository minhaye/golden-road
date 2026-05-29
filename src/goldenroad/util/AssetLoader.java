package goldenroad.util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

public final class AssetLoader {
    private AssetLoader() {
    }

    public static BufferedImage loadImage(String resourcePath) {
        try (InputStream stream = open(resourcePath)) {
            if (stream == null) {
                System.out.println("Khong tim thay resource: " + resourcePath);
                return null;
            }
            return ImageIO.read(stream);
        } catch (IOException e) {
            System.out.println("Loi khi load resource: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream open(String resourcePath) throws IOException {
        String normalizedPath = normalize(resourcePath);

        InputStream classpathStream = AssetLoader.class.getResourceAsStream("/" + normalizedPath);
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

    private static String normalize(String resourcePath) {
        if (resourcePath == null) {
            return "";
        }

        String normalizedPath = resourcePath.replace('\\', '/');
        while (normalizedPath.startsWith("/")) {
            normalizedPath = normalizedPath.substring(1);
        }
        return normalizedPath;
    }
}
