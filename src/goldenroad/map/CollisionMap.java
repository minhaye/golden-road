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
        this.img = ImageIO.read( getClass().getResourceAsStream(path) );

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

    } catch (Exception e) {
        e.printStackTrace();
    }
}

    public boolean isSolid(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return false;
        return solid[x][y];
    }

    public boolean isOneWay(int x, int y) {
    if (x < 0 || y < 0 || x >= width || y >= height) return false;

    int pixel = img.getRGB(x, y);

    int r = (pixel >> 16) & 0xff;
    int g = (pixel >> 8) & 0xff;
    int b = pixel & 0xff;

    return (r < 100 && g > 200 && b < 100); // xanh lá
    }

    
}