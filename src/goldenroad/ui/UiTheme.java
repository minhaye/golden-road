package goldenroad.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.Graphics2D;

import goldenroad.game.GamePanel;

public final class UiTheme {

    public static final int BASE_W = GamePanel.SCREEN_WIDTH;
    public static final int BASE_H = GamePanel.SCREEN_HEIGHT;

    public static final Color OVERLAY_BG = new Color(8, 10, 12, 220);
    public static final Color PANEL_BG = new Color(20, 24, 30, 230);
    public static final Color BAR_BG = new Color(40, 45, 60);
    public static final Color BUTTON_BG = new Color(40, 45, 60);
    public static final Color ACCENT = new Color(120, 140, 180);
    public static final Color TEXT = new Color(220, 220, 235);
    public static final Color TEXT_DIM = new Color(160, 165, 180);
    public static final Color ABOUT_BTN = new Color(70, 80, 100);

    public static final Color HP_FILL = new Color(220, 70, 70);
    public static final Color MP_FILL = new Color(100, 230, 255);

    public static final Color SKILL_GUN = new Color(255, 90, 80);
    public static final Color SKILL_SHOTGUN = new Color(255, 235, 160);
    public static final Color SKILL_DASH = new Color(180, 200, 255);
    public static final Color SKILL_LOCKED = new Color(60, 65, 75);

    public static final Color ITEM_HP = new Color(80, 210, 120);
    public static final Color ITEM_MP = new Color(80, 140, 220);
    public static final Color ITEM_KEY = new Color(230, 190, 70);

    public static final Color COOLDOWN_OVERLAY = new Color(0, 0, 0, 160);

    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 16);
    public static final Font FONT_BUTTON = new Font("SansSerif", Font.BOLD, 20);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_HUD = new Font("SansSerif", Font.BOLD, 11);
    public static final Font FONT_HUD_SMALL = new Font("SansSerif", Font.PLAIN, 10);
    public static final Font FONT_SKILL = new Font("SansSerif", Font.BOLD, 9);

    private UiTheme() {
    }

    public static void enableTextAntialiasing(Graphics2D g2) {
        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );
    }

    public static int[] screenToBuffer(int screenX, int screenY, int panelWidth, int panelHeight) {
        double scaleX = (double) panelWidth / BASE_W;
        double scaleY = (double) panelHeight / BASE_H;
        double scale = Math.min(scaleX, scaleY);

        int renderWidth = (int) (BASE_W * scale);
        int renderHeight = (int) (BASE_H * scale);
        int offsetX = (panelWidth - renderWidth) / 2;
        int offsetY = (panelHeight - renderHeight) / 2;

        int bufferX = (int) ((screenX - offsetX) / scale);
        int bufferY = (int) ((screenY - offsetY) / scale);
        return new int[] { bufferX, bufferY };
    }

    public static void drawRoundPanel(Graphics2D g2, int x, int y, int w, int h, int arc) {
        g2.setColor(PANEL_BG);
        g2.fillRoundRect(x, y, w, h, arc, arc);
        g2.setColor(ACCENT);
        g2.drawRoundRect(x, y, w, h, arc, arc);
    }

    public static void drawBar(Graphics2D g2, int x, int y, int w, int h, int arc,
                               Color fill, float ratio) {
        g2.setColor(BAR_BG);
        g2.fillRoundRect(x, y, w, h, arc, arc);

        int fillW = Math.max(0, Math.min(w, Math.round(w * ratio)));
        if (fillW > 0) {
            g2.setColor(fill);
            g2.fillRoundRect(x, y, fillW, h, arc, arc);
        }

        g2.setColor(ACCENT);
        g2.drawRoundRect(x, y, w, h, arc, arc);
    }
}
