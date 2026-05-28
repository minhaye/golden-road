package goldenroad.render;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

public class ParallaxBackground {

    private BufferedImage[] layers;

    private final double[] speeds = {
        0.05,
        0.2,
        0.4,
        0.8
    };

    public void Load() {

        try {

            layers = new BufferedImage[4];

            for (int i = 0; i < 4; i++) {

                layers[i] = ImageIO.read(
                    getClass().getResourceAsStream(
                        "/assets/background/parallax_layer"
                        + (i + 1)
                        + ".png"
                    )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(
        Graphics2D g2,
        double cameraX,
        double cameraY
    ) {

        for (int i = 0; i < layers.length; i++) {

            BufferedImage layer = layers[i];

            if (layer == null) continue;

            int x = (int)(-cameraX * speeds[i]);
            int y = (int)(-cameraY * speeds[i]);

            g2.drawImage(layer, x, y, null);
        }
    }
}