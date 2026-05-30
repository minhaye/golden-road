package goldenroad.render;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RenderSystem {

    public static final class Viewport {
        private final double scale;
        private final int offsetX;
        private final int offsetY;

        private Viewport(double scale, int offsetX, int offsetY) {
            this.scale = scale;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }

        public double getScale() {
            return scale;
        }

        public int getOffsetX() {
            return offsetX;
        }

        public int getOffsetY() {
            return offsetY;
        }
    }

    private final int baseWidth;
    private final int baseHeight;

    private final BufferedImage buffer;
    private Viewport lastViewport = new Viewport(1.0, 0, 0);

    public RenderSystem(int baseWidth, int baseHeight) {
        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;

        buffer = new BufferedImage(
                baseWidth,
                baseHeight,
                BufferedImage.TYPE_INT_ARGB
        );
    }

    public Graphics2D begin() {
        Graphics2D g2 = buffer.createGraphics();

        // clear screen
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, baseWidth, baseHeight);

        // pixel-perfect
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        return g2;
    }

    public Viewport end(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int windowWidth = g2d.getClipBounds().width;
        int windowHeight = g2d.getClipBounds().height;

        double scale = Math.min(
                (double) windowWidth / baseWidth,
                (double) windowHeight / baseHeight
        );

        int drawWidth = (int) (baseWidth * scale);
        int drawHeight = (int) (baseHeight * scale);

        int offsetX = (windowWidth - drawWidth) / 2;
        int offsetY = (windowHeight - drawHeight) / 2;

        g2d.drawImage(buffer, offsetX, offsetY, drawWidth, drawHeight, null);
        lastViewport = new Viewport(scale, offsetX, offsetY);
        return lastViewport;
    }

    public Viewport getLastViewport() {
        return lastViewport;
    }

    public int getBaseWidth() {
        return baseWidth;
    }

    public int getBaseHeight() {
        return baseHeight;
    }
}