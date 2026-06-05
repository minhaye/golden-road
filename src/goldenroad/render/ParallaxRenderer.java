package goldenroad.render;

import goldenroad.util.AssetLoader;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ParallaxRenderer {
    private static final String[] LAYER_PATHS = {
        "/assets/background/parallax_layer1.png",
        "/assets/background/parallax_layer2.png",
        "/assets/background/parallax_layer3.png",
        "/assets/background/parallax_layer4.png"
    };

    private static final double[] SPEEDS = {0.05, 0.2, 0.4, 0.8};

    private final BufferedImage[] layers = new BufferedImage[LAYER_PATHS.length];

    public void load() {
        for (int i = 0; i < LAYER_PATHS.length; i++) {
            layers[i] = AssetLoader.loadImage(LAYER_PATHS[i]);
        }
    }

    public void render(Graphics2D g2, Camera camera) {
        for (int i = 0; i < layers.length; i++) {
            BufferedImage layer = layers[i];
            if (layer == null) {
                continue;
            }

            int x = (int) (-camera.getX() * SPEEDS[i]);
            int y = (int) (-camera.getY() * SPEEDS[i]);
            g2.drawImage(layer, x, y, null);
        }
    }
}
