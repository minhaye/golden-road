package goldenroad.map;

import goldenroad.util.AssetLoader;

import java.awt.image.BufferedImage;

public class CollisionMap {

    private boolean[][] solid;
    private boolean[][] laser;
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
                laser = null;
                return;
            }

            width = img.getWidth();
            height = img.getHeight();
            solid = new boolean[width][height];
            laser = new boolean[width][height];

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = img.getRGB(x, y);
                    int r = (pixel >> 16) & 0xff;
                    int g = (pixel >> 8) & 0xff;
                    int b = pixel & 0xff;

                    boolean whiteSolid = r > 250 && g > 250 && b > 250;
                    boolean redLaser = r > 200 && g < 80 && b < 80;

                    laser[x][y] = redLaser;
                    solid[x][y] = whiteSolid || redLaser;
                }
            }
        } catch (RuntimeException e) {
            width = 0;
            height = 0;
            solid = null;
            laser = null;
            img = null;
            e.printStackTrace();
        }
    }

    public boolean isSolid(int x, int y) {
        if (solid == null || x < 0 || y < 0 || x >= width || y >= height) {
            return false;
        }
        return solid[x][y];
    }

    public boolean isLaser(int x, int y) {
        if (laser == null || x < 0 || y < 0 || x >= width || y >= height) {
            return false;
        }
        return laser[x][y];
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

    public boolean isAreaSolid(double x, double y, double areaWidth, double areaHeight) {
        if (solid == null || areaWidth <= 0 || areaHeight <= 0) {
            return false;
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
                if (solid[tx][ty]) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isAreaLaser(double x, double y, double areaWidth, double areaHeight) {
        if (laser == null || areaWidth <= 0 || areaHeight <= 0) {
            return false;
        }

        int left = (int)Math.floor(x);
        int right = (int)Math.floor(x + areaWidth - 1);
        int top = (int)Math.floor(y);
        int bottom = (int)Math.floor(y + areaHeight - 1);

        if (left < 0 || top < 0 || right >= width || bottom >= height) {
            return false;
        }

        for (int tx = left; tx <= right; tx++) {
            for (int ty = top; ty <= bottom; ty++) {
                if (laser[tx][ty]) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean canStandAt(double x, double y, double entityWidth, double entityHeight) {
        if (solid == null) {
            return true;
        }
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

    public boolean isSegmentBlocked(double startX, double startY, double endX, double endY, int areaWidth, int areaHeight) {
        if (solid == null) {
            return false;
        }

        double dx = endX - startX;
        double dy = endY - startY;
        int steps = Math.max(1, (int)Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)) / 8.0));

        for (int i = 0; i <= steps; i++) {
            double t = (double)i / steps;
            int x = (int)Math.round(startX + dx * t - areaWidth / 2.0);
            int y = (int)Math.round(startY + dy * t - areaHeight / 2.0);

            if (isAreaSolid(x, y, areaWidth, areaHeight)) {
                return true;
            }
        }

        return false;
    }

    public boolean isOneWay(int x, int y) {
        if (img == null || x < 0 || y < 0 || x >= width || y >= height) {
            return false;
        }

        int pixel = img.getRGB(x, y);
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = pixel & 0xff;

        return r < 100 && g > 200 && b < 100;
    }
}
