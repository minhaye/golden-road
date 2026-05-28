package goldenroad.map;

import goldenroad.util.AssetLoader;

import java.awt.image.BufferedImage;

public class CollisionMap {

    private boolean[][] solid;
    private int width;
    private int height;
    private BufferedImage img;


public void load(String path) {
    try {
        this.img = AssetLoader.loadImage(path);
        if (img == null) {
            width = 0;
            height = 0;
            solid = null;
            return;
        }

        width = img.getWidth();
        height = img.getHeight();
        solid = new boolean[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                int pixel = img.getRGB(x, y);
                

                int r = (pixel >> 16) & 0xff;
                int g = (pixel >> 8) & 0xff;
                int b = pixel & 0xff;

                // 👉 CHỌN 1 kiểu duy nhất:
                // trắng = solid
                solid[x][y] = (r > 250 && g > 250 && b > 250);
            }
        }

    } catch (RuntimeException e) {
        e.printStackTrace();
    }
}

    public boolean isSolid(int x, int y) {
        if (solid == null) return false;
        if (x < 0 || y < 0 || x >= width || y >= height) return false;
        return solid[x][y];
    }

    public boolean isLoaded() {
        return solid != null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isAreaSolid(int x, int y, int areaWidth, int areaHeight) {
        if (solid == null || areaWidth <= 0 || areaHeight <= 0) {
            return false;
        }

        if (x < 0 || y < 0 || x + areaWidth > width || y + areaHeight > height) {
            return true;
        }

        int right = x + areaWidth;
        int bottom = y + areaHeight;

        for (int tx = x; tx < right; tx++) {
            for (int ty = y; ty < bottom; ty++) {
                if (solid[tx][ty]) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isSegmentBlocked(double startX, double startY, double endX, double endY, int areaWidth, int areaHeight) {
        if (solid == null) {
            return false;
        }

        double dx = endX - startX;
        double dy = endY - startY;
        int steps = Math.max(1, (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / 8.0));

        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int x = (int) Math.round(startX + dx * t - areaWidth / 2.0);
            int y = (int) Math.round(startY + dy * t - areaHeight / 2.0);

            if (isAreaSolid(x, y, areaWidth, areaHeight)) {
                return true;
            }
        }

        return false;
    }

    public boolean isOneWay(int x, int y) {
    if (img == null) return false;
    if (x < 0 || y < 0 || x >= width || y >= height) return false;

    int pixel = img.getRGB(x, y);

    int r = (pixel >> 16) & 0xff;
    int g = (pixel >> 8) & 0xff;
    int b = pixel & 0xff;

    return (r < 100 && g > 200 && b < 100); // xanh lá
    }

    
}
