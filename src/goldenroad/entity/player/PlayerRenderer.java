package goldenroad.entity.player;

import goldenroad.util.AssetLoader;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class PlayerRenderer {
    private final Player player;
    private BufferedImage frontArmSpriteSheet;
    private BufferedImage noFrontArmSpriteSheet;
    private BufferedImage gunSpriteSheet;

    private static final int FRAME_W = 96;
    private static final int FRAME_H = 96;
    private static final int DRAW_OFFSET_X = -38;
    private static final int DRAW_OFFSET_Y = -46;
    private static final int GUN_W = 32;
    private static final int GUN_H = 16;
    private static final double GUN_PIVOT_X = 1.0;
    private static final double GUN_PIVOT_Y = 8.0;
    private static final double PLAYER_GUN_PIVOT_Y = 9.0;
    private static final double GUN_DIRECTION_OFFSET_X = 5.0;
    private static final double RUN_GUN_OFFSET_X = 5.0;
    private static final double RUN_GUN_OFFSET_Y = 4.0;

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
        frontArmSpriteSheet = AssetLoader.loadImage("/assets/player/MIRAI_WITH_FRONT_ARM.png");
        noFrontArmSpriteSheet = AssetLoader.loadImage("/assets/player/MIRAI_WITH_NO_FRONT_ARM.png");
        gunSpriteSheet = AssetLoader.loadImage("/assets/player/MIRAI_GUN.png");
        if (frontArmSpriteSheet != null) {
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

    private BufferedImage getFrame(BufferedImage sheet) {
        if (sheet == null) {
            return null;
        }

        int x = currentFrame * FRAME_W;
        int y = player.getAnimationRow() * FRAME_H;
        if (x + FRAME_W > sheet.getWidth() || y + FRAME_H > sheet.getHeight()) {
            return null;
        }

        return sheet.getSubimage(x, y, FRAME_W, FRAME_H);
    }

    private BufferedImage getGunSprite() {
        if (gunSpriteSheet == null || gunSpriteSheet.getWidth() < GUN_W || gunSpriteSheet.getHeight() < GUN_H) {
            return null;
        }

        return gunSpriteSheet.getSubimage(0, 0, GUN_W, GUN_H);
    }

    public void render(Graphics2D g) {
        render(g, false, 0, 0);
    }

    public void render(Graphics2D g, boolean aiming, double aimX, double aimY) {
        BufferedImage img = getFrame(aiming ? noFrontArmSpriteSheet : frontArmSpriteSheet);

        if (img == null) {
            g.setColor(new Color(230, 190, 70));
            g.fillRect((int) player.getX(), (int) player.getY(), (int) player.getWidth(), (int) player.getHeight());
            return;
        }

        int drawX = (int) player.getX() + DRAW_OFFSET_X;
        int drawY = (int) player.getY() + DRAW_OFFSET_Y;
        if (player.getDirection() == 1) {
            g.drawImage(img, drawX, drawY, FRAME_W, FRAME_H, null);
        } else {
            g.drawImage(img, drawX + FRAME_W, drawY, -FRAME_W, FRAME_H, null);
        }

        if (aiming) {
            renderGun(g, aimX, aimY);
        }
    }

    private void renderGun(Graphics2D g, double aimX, double aimY) {
        BufferedImage gunSprite = getGunSprite();
        if (gunSprite == null) {
            return;
        }

        Point2D.Double pivot = getGunPivot();
        double angle = Math.atan2(aimY - pivot.y, aimX - pivot.x);
        boolean aimLeft = Math.cos(angle) < 0;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g2.translate(pivot.x, pivot.y);
        g2.rotate(angle);
        if (aimLeft) {
            g2.scale(1, -1);
        }
        g2.drawImage(gunSprite, (int) -GUN_PIVOT_X, (int) -GUN_PIVOT_Y, GUN_W, GUN_H, null);
        g2.dispose();
    }

    public Point2D.Double getGunCenter(double aimX, double aimY) {
        Point2D.Double pivot = getGunPivot();
        double angle = Math.atan2(aimY - pivot.y, aimX - pivot.x);
        boolean aimLeft = Math.cos(angle) < 0;
        double localCenterX = GUN_W * 0.5 - GUN_PIVOT_X;
        double localCenterY = GUN_H * 0.5 - GUN_PIVOT_Y;
        if (aimLeft) {
            localCenterY = -localCenterY;
        }

        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Point2D.Double(
                pivot.x + localCenterX * cos - localCenterY * sin,
                pivot.y + localCenterX * sin + localCenterY * cos
        );
    }

    private Point2D.Double getGunPivot() {
        double directionOffset = player.getDirection() == 1 ? GUN_DIRECTION_OFFSET_X : -GUN_DIRECTION_OFFSET_X;
        double pivotX = player.getX() + player.getWidth() * 0.5 + directionOffset;
        double pivotY = player.getY() + PLAYER_GUN_PIVOT_Y;
        if (Math.abs(player.getVelocityX()) > 0.1) {
            pivotX += Math.signum(player.getVelocityX()) * RUN_GUN_OFFSET_X;
            pivotY += RUN_GUN_OFFSET_Y;
        }

        return new Point2D.Double(Math.round(pivotX), Math.round(pivotY));
    }
}
