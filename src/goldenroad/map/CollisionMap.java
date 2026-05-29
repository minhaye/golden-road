package goldenroad.map;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class CollisionMap {

    private boolean[][] solid;
    private int width;
    private int height;
    private BufferedImage img;

    public void load(String path) {
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream == null) {
                throw new IllegalArgumentException("Missing collision map: " + path);
            }

            this.img = ImageIO.read(stream);
            width = img.getWidth();
            height = img.getHeight();
            solid = new boolean[width][height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = img.getRGB(x, y);
                    int r = (pixel >> 16) & 0xff;
                    int g = (pixel >> 8) & 0xff;
                    int b = pixel & 0xff;

                    solid[x][y] = (r > 250 && g > 250 && b > 250);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isSolid(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height || solid == null) {
            return false;
        }
        return solid[x][y];
    }

    public boolean isOneWay(int x, int y) {
        if (img == null || x < 0 || y < 0 || x >= width || y >= height) {
            return false;
        }

        int pixel = img.getRGB(x, y);
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = pixel & 0xff;

        return (r < 100 && g > 200 && b < 100);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isAreaSolid(double x, double y, double areaWidth, double areaHeight) {
        if (solid == null) {
            return true;
        }

        int left = (int)Math.floor(x);
        int right = (int)Math.floor(x + areaWidth - 1);
        int top = (int)Math.floor(y);
        int bottom = (int)Math.floor(y + areaHeight - 1);

        if (left < 0 || top < 0 || right >= width || bottom >= height) {
            return true;
        }

        for (int tx = left; tx <= right; tx++) {
            for (int ty = top; ty <= bottom; ty++) {
                if (isSolid(tx, ty)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean canStandAt(double x, double y, double entityWidth, double entityHeight) {
        if (isAreaSolid(x, y, entityWidth, entityHeight)) {
            return false;
        }

        int left = (int)Math.floor(x + 2);
        int right = (int)Math.floor(x + entityWidth - 3);
        int bottom = (int)Math.floor(y + entityHeight);
        int supportDepth = 6;

        for (int tx = left; tx <= right; tx++) {
            for (int ty = bottom; ty <= bottom + supportDepth; ty++) {
                if (isSolid(tx, ty) || isOneWay(tx, ty)) {
                    return true;
                }
            }
        }

        return false;
    }
}
