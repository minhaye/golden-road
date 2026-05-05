package goldenroad.scene;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;

import goldenroad.game.GamePanel;
import goldenroad.input.MouseHandler;

public class Menu {
    private final GamePanel panel;

    private boolean active = true; // show menu at start
    private boolean aboutOpen = false;

    private Rectangle[] buttons;
    private String[] labels = {"Bắt đầu", "Tiếp tục", "Cài đặt", "Exit"};
    private Rectangle aboutButton;
    private Rectangle aboutSidebar;
    private int lastW = -1;
    private int lastH = -1;

    public Menu(GamePanel panel) {
        this.panel = panel;
        layoutElements();
    }

    private void layoutElements() {
        int w = panel.getWidth();
        int h = panel.getHeight();

        int btnW = Math.min(300, w / 3);
        int btnH = 48;
        int gap = 18;

        int totalH = (btnH * labels.length) + (gap * (labels.length - 1));
        int startY = Math.max(50, (h / 2) - (totalH / 2));
        int centerX = w / 2;

        buttons = new Rectangle[labels.length];
        for (int i = 0; i < labels.length; i++) {
            int x = centerX - (btnW / 2);
            int y = startY + i * (btnH + gap);
            buttons[i] = new Rectangle(x, y, btnW, btnH);
        }

        int aboutSize = 36;
        aboutButton = new Rectangle(w - aboutSize - 12, h - aboutSize - 12, aboutSize, aboutSize);

        aboutSidebar = new Rectangle(w - 320, 60, 300, h - 120);
    }

    public boolean isActive() {
        return active;
    }

    public void update(MouseHandler mouse) {
        // relayout when panel size changes
        if (panel.getWidth() != lastW || panel.getHeight() != lastH || buttons == null) {
            layoutElements();
            lastW = panel.getWidth();
            lastH = panel.getHeight();
        }

        if (mouse.isLeftClickQueued()) {
            int mx = mouse.getMouseX();
            int my = mouse.getMouseY();

            // About button (bottom-right)
            if (aboutButton.contains(mx, my)) {
                aboutOpen = !aboutOpen;
                mouse.consumeLeftClick();
                return;
            }

            if (!active) return;

            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i].contains(mx, my)) {
                    handleButton(i);
                    mouse.consumeLeftClick();
                    return;
                }
            }
        }
    }

    private void handleButton(int index) {
        switch (index) {
            case 0: // Bắt đầu
                active = false;
                break;
            case 1: // Tiếp tục
                active = false;
                break;
            case 2: // Cài đặt (placeholder)
                // toggle settings or show dialog later
                break;
            case 3: // Exit
                System.exit(0);
                break;
        }
    }

    public void render(Graphics2D g2) {
        // relayout when panel size changes
        if (panel.getWidth() != lastW || panel.getHeight() != lastH || buttons == null) {
            layoutElements();
            lastW = panel.getWidth();
            lastH = panel.getHeight();
        }
        // ensure sharp fonts
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // draw menu overlay
        if (active) {
            // translucent background
            g2.setColor(new Color(8, 10, 12, 200));
            g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());

            Font font = new Font("SansSerif", Font.BOLD, 20);
            g2.setFont(font);

            for (int i = 0; i < buttons.length; i++) {
                Rectangle b = buttons[i];
                g2.setColor(new Color(40, 45, 60));
                g2.fillRect(b.x, b.y, b.width, b.height);
                g2.setColor(new Color(200, 200, 220));
                int strW = g2.getFontMetrics().stringWidth(labels[i]);
                int tx = b.x + (b.width - strW) / 2;
                int ty = b.y + (b.height + g2.getFontMetrics().getAscent()) / 2 - 4;
                g2.drawString(labels[i], tx, ty);
            }
        }

        // about button (always visible)
        g2.setColor(new Color(70, 80, 100));
        g2.fillRect(aboutButton.x, aboutButton.y, aboutButton.width, aboutButton.height);
        g2.setColor(new Color(220, 220, 240));
        g2.drawString("i", aboutButton.x + 12, aboutButton.y + 24);

        // about sidebar
        if (aboutOpen) {
            g2.setColor(new Color(20, 24, 30, 230));
            g2.fillRect(aboutSidebar.x, aboutSidebar.y, aboutSidebar.width, aboutSidebar.height);
            g2.setColor(new Color(220, 220, 230));
            Font h = new Font("SansSerif", Font.BOLD, 16);
            g2.setFont(h);
            int x = aboutSidebar.x + 12;
            int y = aboutSidebar.y + 26;
            g2.drawString("Thông tin dev:", x, y);
            Font small = new Font("SansSerif", Font.PLAIN, 12);
            g2.setFont(small);
            int sy = y + 18;
            g2.drawString("Dev: Nhóm Golden Road", x, sy);
            g2.drawString("Hướng dẫn: WASD di chuyển, chuột bắn", x, sy + 18);
            g2.drawString("Cốt truyện: Một hành trình bí ẩn...", x, sy + 36);
        }
    }
}
