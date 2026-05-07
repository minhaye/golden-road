package goldenroad.scene;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import goldenroad.game.GamePanel;
import goldenroad.input.MouseHandler;

public class Menu {

    private final GamePanel panel;

    private boolean active = true;
    private boolean aboutOpen = false;

    private Rectangle[] buttons;

    private final String[] labels = {
        "Start",
        "Continue",
        "Settings",
        "Exit"
    };

    private Rectangle aboutButton;
    private Rectangle aboutSidebar;

    // ===== GAME SPACE =====
    // dùng resolution gốc của game thay vì panel size thật
    private static final int BASE_W = GamePanel.SCREEN_WIDTH;
    private static final int BASE_H = GamePanel.SCREEN_HEIGHT;

    public Menu(GamePanel panel) {
        this.panel = panel;
        layoutElements();
    }

    // =========================================================
    // LAYOUT THEO GAME BUFFER
    // =========================================================
    private void layoutElements() {

        int w = BASE_W;
        int h = BASE_H;

        int btnW = 220;
        int btnH = 48;
        int gap  = 18;

        int totalH =
            (btnH * labels.length)
            + (gap * (labels.length - 1));

        int startY =
            (h / 2) - (totalH / 2);

        int centerX = w / 2;

        buttons = new Rectangle[labels.length];

        for (int i = 0; i < labels.length; i++) {

            int x = centerX - (btnW / 2);
            int y = startY + i * (btnH + gap);

            buttons[i] =
                new Rectangle(x, y, btnW, btnH);
        }

        int aboutSize = 36;

        aboutButton = new Rectangle(
            w - aboutSize - 12,
            h - aboutSize - 12,
            aboutSize,
            aboutSize
        );

        aboutSidebar = new Rectangle(
            w - 320,
            40,
            300,
            h - 80
        );
    }

    // =========================================================
    // ACTIVE
    // =========================================================
    public boolean isActive() {
        return active;
    }

    // =========================================================
    // UPDATE
    // =========================================================
    public void update(MouseHandler mouse) {

        // ===== convert mouse screen -> game buffer =====
        double scaleX =
            (double) panel.getWidth() / BASE_W;

        double scaleY =
            (double) panel.getHeight() / BASE_H;

        double scale = Math.min(scaleX, scaleY);

        int renderWidth  = (int)(BASE_W * scale);
        int renderHeight = (int)(BASE_H * scale);

        int offsetX =
            (panel.getWidth() - renderWidth) / 2;

        int offsetY =
            (panel.getHeight() - renderHeight) / 2;

        int mx = (int)(
            (mouse.getMouseX() - offsetX) / scale
        );

        int my = (int)(
            (mouse.getMouseY() - offsetY) / scale
        );

        // ===== click =====
        if (mouse.consumeLeftJustPressed()) {

            // ===== ABOUT BUTTON =====
            if (aboutButton.contains(mx, my)) {

                aboutOpen = !aboutOpen;
                return;
            }

            if (!active) return;

            // ===== MENU BUTTONS =====
            for (int i = 0; i < buttons.length; i++) {

                if (buttons[i].contains(mx, my)) {

                    handleButton(i);
                    return;
                }
            }
        }
    }

    // =========================================================
    // BUTTON LOGIC
    // =========================================================
    private void handleButton(int index) {

        switch (index) {

            case 0:
                active = false;
                break;

            case 1:
                active = false;
                break;

            case 2:
                // settings
                break;

            case 3:
                System.exit(0);
                break;
        }
    }

    // =========================================================
    // RENDER
    // =========================================================
    public void render(Graphics2D g2) {

        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        // ===== BACKGROUND =====
        if (active) {

            g2.setColor(
                new Color(8, 10, 12, 220)
            );

            g2.fillRect(0, 0, BASE_W, BASE_H);

            Font font =
                new Font("SansSerif", Font.BOLD, 20);

            g2.setFont(font);

            for (int i = 0; i < buttons.length; i++) {

                Rectangle b = buttons[i];

                // button body
                g2.setColor(
                    new Color(40, 45, 60)
                );

                g2.fillRoundRect(
                    b.x,
                    b.y,
                    b.width,
                    b.height,
                    14,
                    14
                );

                // border
                g2.setColor(
                    new Color(120, 140, 180)
                );

                g2.drawRoundRect(
                    b.x,
                    b.y,
                    b.width,
                    b.height,
                    14,
                    14
                );

                // text
                g2.setColor(
                    new Color(220, 220, 235)
                );

                int strW =
                    g2.getFontMetrics()
                      .stringWidth(labels[i]);

                int tx =
                    b.x + (b.width - strW) / 2;

                int ty =
                    b.y
                    + (b.height / 2)
                    + 7;

                g2.drawString(labels[i], tx, ty);
            }
        

        // =====================================================
        // ABOUT BUTTON
        // =====================================================

        g2.setColor(
            new Color(70, 80, 100)
        );

        g2.fillRoundRect(
            aboutButton.x,
            aboutButton.y,
            aboutButton.width,
            aboutButton.height,
            10,
            10
        );

        g2.setColor(
            new Color(230, 230, 240)
        );

        g2.setFont(
            new Font("SansSerif", Font.BOLD, 18)
        );

        g2.drawString(
            "i",
            aboutButton.x + 13,
            aboutButton.y + 24
        );

        // =====================================================
        // ABOUT PANEL
        // =====================================================

        if (aboutOpen) {

            g2.setColor(
                new Color(20, 24, 30, 240)
            );

            g2.fillRoundRect(
                aboutSidebar.x,
                aboutSidebar.y,
                aboutSidebar.width,
                aboutSidebar.height,
                18,
                18
            );

            g2.setColor(
                new Color(230, 230, 240)
            );

            Font header =
                new Font("Ariel", Font.BOLD, 16);

            g2.setFont(header);

            int x = aboutSidebar.x + 16;
            int y = aboutSidebar.y + 28;

            g2.drawString(
                "Thông tin dev:",
                x,
                y
            );

            Font small =
                new Font("SansSerif", Font.PLAIN, 12);

            g2.setFont(small);

            int sy = y + 24;

            g2.drawString(
                "Dev: Nhóm 23",
                x,
                sy
            );

            g2.drawString(
                "WASD để di chuyển",
                x,
                sy + 20
            );

            g2.drawString(
                "Chuột trái để bắn đạn thường",
                x,
                sy + 40
            );

            g2.drawString(
                "Chuột phải để shotgun",
                x,
                sy + 60
            );

            g2.drawString(
                "Shift để Dash (lướt + né đòn)",
                x,
                sy + 80
            );
        }
        }
    }
}