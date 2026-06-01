package goldenroad.ui;

import goldenroad.entity.player.Player;
import goldenroad.game.GameWorld;
import goldenroad.map.MapId;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import goldenroad.entity.monster.Monster;
import goldenroad.scene.SceneManager;

public class GameOverlayRenderer {
    private static final int MINIMAP_MAX_WIDTH = 160;
    private static final int MINIMAP_MAX_HEIGHT = 110;
    private static final int MINIMAP_PADDING = 12;

    public void renderToast(Graphics2D g2, String toastMessage) {
        if (toastMessage == null || toastMessage.isBlank()) {
            return;
        }

        UiTheme.enableTextAntialiasing(g2);
        g2.setComposite(AlphaComposite.SrcOver);

        int panelWidth = UiTheme.BASE_W;
        int panelHeight = UiTheme.BASE_H;

        g2.setFont(UiTheme.FONT_BODY);
        int textWidth = g2.getFontMetrics().stringWidth(toastMessage);
        int boxWidth = Math.max(180, textWidth + 24);
        int boxHeight = 28;
        int x = (panelWidth - boxWidth) / 2;
        int y = panelHeight - 42;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x, y, boxWidth, boxHeight, 12, 12);

        g2.setColor(UiTheme.TEXT);
        g2.drawString(toastMessage, x + (boxWidth - textWidth) / 2, y + 19);
    }

    public void renderMinimap(Graphics2D g2, GameWorld world, SceneManager sceneManager, Player player, double cameraX, double cameraY, int viewportWidth, int viewportHeight) {
        if (world == null || sceneManager == null || player == null || world.getWorldWidth() <= 0 || world.getWorldHeight() <= 0) {
            return;
        }

        int miniX = viewportWidth - MINIMAP_MAX_WIDTH - MINIMAP_PADDING;
        int miniY = MINIMAP_PADDING;

        double scale = Math.min(
            (double) MINIMAP_MAX_WIDTH / world.getWorldWidth(),
            (double) MINIMAP_MAX_HEIGHT / world.getWorldHeight()
        );
        scale = Math.max(0.02, Math.min(scale, 1.0));

        int miniW = Math.max(1, (int) Math.round(world.getWorldWidth() * scale));
        int miniH = Math.max(1, (int) Math.round(world.getWorldHeight() * scale));

        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(new Color(10, 14, 18, 210));
        g2.fillRoundRect(miniX - 4, miniY - 4, miniW + 8, miniH + 28, 12, 12);

        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRoundRect(miniX, miniY, miniW, miniH, 8, 8);

        g2.setColor(new Color(120, 140, 180, 180));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(miniX, miniY, miniW, miniH, 8, 8);

        if (world.getMapImage() != null) {
            g2.drawImage(world.getMapImage(), miniX, miniY, miniW, miniH, null);
        }

        if (sceneManager.getCurrentScreen() != null) {
            g2.setColor(new Color(255, 170, 70, 200));
            for (Monster monster : sceneManager.getCurrentScreen().getMonsters()) {
                if (monster == null || monster.isDead()) {
                    continue;
                }

                double monsterRatioX = clampDouble(monster.getX() / Math.max(1.0, world.getWorldWidth()), 0.0, 1.0);
                double monsterRatioY = clampDouble(monster.getY() / Math.max(1.0, world.getWorldHeight()), 0.0, 1.0);
                int monsterX = miniX + (int) Math.round(monsterRatioX * miniW);
                int monsterY = miniY + (int) Math.round(monsterRatioY * miniH);
                g2.fill(new Ellipse2D.Double(monsterX - 2, monsterY - 2, 4, 4));
            }
        }

        double playerRatioX = clampDouble(player.getX() / Math.max(1.0, world.getWorldWidth()), 0.0, 1.0);
        double playerRatioY = clampDouble(player.getY() / Math.max(1.0, world.getWorldHeight()), 0.0, 1.0);
        int playerX = miniX + (int) Math.round(playerRatioX * miniW);
        int playerY = miniY + (int) Math.round(playerRatioY * miniH);

        int viewX = miniX + (int) Math.round((cameraX / Math.max(1.0, world.getWorldWidth())) * miniW);
        int viewY = miniY + (int) Math.round((cameraY / Math.max(1.0, world.getWorldHeight())) * miniH);
        int viewW = (int) Math.round((viewportWidth / Math.max(1.0, world.getWorldWidth())) * miniW);
        int viewH = (int) Math.round((viewportHeight / Math.max(1.0, world.getWorldHeight())) * miniH);
        viewW = Math.max(2, Math.min(viewW, miniW));
        viewH = Math.max(2, Math.min(viewH, miniH));

        viewX = Math.max(miniX, Math.min(viewX, miniX + miniW - viewW));
        viewY = Math.max(miniY, Math.min(viewY, miniY + miniH - viewH));

        g2.setColor(new Color(255, 255, 255, 120));
        g2.drawRect(viewX, viewY, viewW, viewH);

        g2.setColor(new Color(255, 90, 80));
        g2.fillOval(playerX - 3, playerY - 3, 6, 6);
        g2.setColor(Color.WHITE);
        g2.drawOval(playerX - 3, playerY - 3, 6, 6);

        g2.setFont(UiTheme.FONT_HUD_SMALL);
        g2.setColor(UiTheme.TEXT);
        MapId mapId = world.getCurrentMapId();
        String label = mapId == null ? "MAP ?" : mapId.displayName();
        g2.drawString(label, miniX + 8, miniY + miniH + 16);
    }

    private double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
