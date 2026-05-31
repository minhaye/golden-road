package goldenroad.entity.player;

import goldenroad.util.AssetLoader;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PlayerRenderer {
    private final Player player;
    private BufferedImage spriteSheet;

    private static final int FRAME_W = 96;
    private static final int FRAME_H = 96;
    private static final int DRAW_OFFSET_X = -38;
    private static final int DRAW_OFFSET_Y = -46;

    private int currentFrame = 0;
    private int animationTick = 0;
    private static final int ANIMATION_SPEED = 5;

    private int prevStateKey;

    public PlayerRenderer(Player player) {
        this.player = player;
        this.prevStateKey = player.getAnimationStateKey();
        loadSprite();
    }

    private void loadSprite() {
        spriteSheet = AssetLoader.loadImage("/assets/player/MIRAI_WITH_FRONT_ARM.png");
        if (spriteSheet != null) {
            System.out.println("Load sprite thanh cong.");
        }
    }

    public void updateAnimation() {
        int stateKey = player.getAnimationStateKey();

        if (stateKey != prevStateKey) {
            currentFrame = 0;
            animationTick = 0;
            prevStateKey = stateKey;
        }

        animationTick++;
        if (animationTick >= ANIMATION_SPEED) {
            animationTick = 0;
            currentFrame++;

            int max = player.getAnimationFrameCount();
            if (currentFrame >= max) currentFrame = 0;
        }
    }

    private BufferedImage getFrame() {
        if (spriteSheet == null) {
            return null;
        }

        int x = currentFrame * FRAME_W;
        int y = player.getAnimationRow() * FRAME_H;
        if (x + FRAME_W > spriteSheet.getWidth() || y + FRAME_H > spriteSheet.getHeight()) {
            return null;
        }

        return spriteSheet.getSubimage(x, y, FRAME_W, FRAME_H);
    }

    public void render(Graphics2D g) {
        BufferedImage img = getFrame();

        if (img == null) {
            g.setColor(new Color(230, 190, 70));
            g.fillRect((int) player.getX(), (int) player.getY(), (int) player.getWidth(), (int) player.getHeight());
            return;
        }

        if (player.getDirection() == 1) {
            g.drawImage(img, (int) player.getX() + DRAW_OFFSET_X, (int) player.getY() + DRAW_OFFSET_Y, FRAME_W, FRAME_H, null);
        } else {
            g.drawImage(img, (int) player.getX() + DRAW_OFFSET_X + FRAME_W, (int) player.getY() + DRAW_OFFSET_Y,
                    -FRAME_W, FRAME_H, null);
        }
    }
}
