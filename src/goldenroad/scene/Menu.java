package goldenroad.scene;

import goldenroad.game.GamePanel;
import goldenroad.input.MouseHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

public class Menu {

    private final GamePanel panel;

    private boolean active = true;
    private boolean aboutOpen = false;
    private boolean settingsOpen = false;

    private Rectangle[] buttons;

    private final String[] labels = {
            "Start",
            "Continue",
            "Settings",
            "Exit"
    };

    private Rectangle aboutButton;
    private Rectangle aboutSidebar;
    private Rectangle settingsPanel;
    private Rectangle musicSliderTrack;
    private Rectangle sfxSliderTrack;
    private Rectangle musicMuteButton;
    private Rectangle sfxMuteButton;
    private Rectangle settingsBackButton;

    private int draggingSlider = 0;
    private float lastMusicVolume = 0.6f;
    private float lastSfxVolume = 0.8f;

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
        int gap = 18;

        int totalH = (btnH * labels.length)
                + (gap * (labels.length - 1));

        int startY = (h / 2) - (totalH / 2);

        int centerX = w / 2;

        buttons = new Rectangle[labels.length];

        for (int i = 0; i < labels.length; i++) {

            int x = centerX - (btnW / 2);
            int y = startY + i * (btnH + gap);

            buttons[i] = new Rectangle(x, y, btnW, btnH);
        }

        int aboutSize = 36;

        aboutButton = new Rectangle(
                w - aboutSize - 12,
                h - aboutSize - 12,
                aboutSize,
                aboutSize);

        aboutSidebar = new Rectangle(
                w - 320,
                40,
                300,
                h - 80);

        settingsPanel = new Rectangle(
                centerX - 190,
                48,
                380,
                264);

        int sliderX = settingsPanel.x + 34;
        int muteX = sliderX + 248;

        musicSliderTrack = new Rectangle(
                sliderX,
                settingsPanel.y + 92,
                230,
                8);

        sfxSliderTrack = new Rectangle(
                sliderX,
                settingsPanel.y + 154,
                230,
                8);

        musicMuteButton = new Rectangle(
                muteX,
                musicSliderTrack.y - 14,
                82,
                30);

        sfxMuteButton = new Rectangle(
                muteX,
                sfxSliderTrack.y - 14,
                82,
                30);

        settingsBackButton = new Rectangle(
                centerX - 55,
                settingsPanel.y + settingsPanel.height - 46,
                110,
                34);
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
        double scaleX = (double) panel.getWidth() / BASE_W;

        double scaleY = (double) panel.getHeight() / BASE_H;

        double scale = Math.min(scaleX, scaleY);

        int renderWidth = (int) (BASE_W * scale);
        int renderHeight = (int) (BASE_H * scale);

        int offsetX = (panel.getWidth() - renderWidth) / 2;

        int offsetY = (panel.getHeight() - renderHeight) / 2;

        int mx = (int) ((mouse.getMouseX() - offsetX) / scale);

        int my = (int) ((mouse.getMouseY() - offsetY) / scale);

        if (settingsOpen) {
            handleSettingsInput(mouse, mx, my);
            return;
        }

        // ===== click =====
        if (mouse.consumeLeftJustPressed()) {

            // ===== ABOUT BUTTON =====
            if (aboutButton.contains(mx, my)) {

                aboutOpen = !aboutOpen;
                return;
            }

            if (!active)
                return;

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
        panel.playMenuClickSound();
        switch (index) {

            case 0:
                active = false;
                break;

            case 1:
                active = false;
                break;

            case 2:
                aboutOpen = false;
                settingsOpen = true;
                break;

            case 3:
                System.exit(0);
                break;
        }
    }

    private void handleSettingsInput(MouseHandler mouse, int mx, int my) {
        if (!mouse.isLeftPressed()) {
            draggingSlider = 0;
        }

        if (mouse.consumeLeftJustPressed()) {
            if (settingsBackButton.contains(mx, my)) {
                panel.playMenuClickSound();
                settingsOpen = false;
                return;
            }

            if (musicMuteButton.contains(mx, my)) {
                panel.playMenuClickSound();
                toggleMusicMute();
                return;
            }

            if (sfxMuteButton.contains(mx, my)) {
                panel.playMenuClickSound();
                toggleSfxMute();
                return;
            }

            if (getSliderHitBox(musicSliderTrack).contains(mx, my)) {
                draggingSlider = 1;
            } else if (getSliderHitBox(sfxSliderTrack).contains(mx, my)) {
                draggingSlider = 2;
            }
        }

        if (mouse.isLeftPressed()) {
            if (draggingSlider == 1) {
                panel.setMusicVolume(getSliderValue(musicSliderTrack, mx));
            } else if (draggingSlider == 2) {
                panel.setSfxVolume(getSliderValue(sfxSliderTrack, mx));
            }
        }
    }

    private void toggleMusicMute() {
        float volume = panel.getMusicVolume();
        if (volume > 0.01f) {
            lastMusicVolume = volume;
            panel.setMusicVolume(0f);
        } else {
            panel.setMusicVolume(lastMusicVolume);
        }
    }

    private void toggleSfxMute() {
        float volume = panel.getSfxVolume();
        if (volume > 0.01f) {
            lastSfxVolume = volume;
            panel.setSfxVolume(0f);
        } else {
            panel.setSfxVolume(lastSfxVolume);
        }
    }

    private Rectangle getSliderHitBox(Rectangle track) {
        return new Rectangle(track.x - 8, track.y - 12, track.width + 16, track.height + 24);
    }

    private float getSliderValue(Rectangle track, int mouseX) {
        float value = (mouseX - track.x) / (float) track.width;
        return Math.max(0f, Math.min(1f, value));
    }

    // =========================================================
    // RENDER
    // =========================================================
    public void render(Graphics2D g2) {

        g2.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ===== BACKGROUND =====
        if (active) {

            g2.setColor(
                    new Color(8, 10, 12, 220));

            g2.fillRect(0, 0, BASE_W, BASE_H);

            if (settingsOpen) {
                renderSettings(g2);
                return;
            }

            Font font = new Font("SansSerif", Font.BOLD, 20);

            g2.setFont(font);

            for (int i = 0; i < buttons.length; i++) {

                Rectangle b = buttons[i];

                // button body
                g2.setColor(
                        new Color(40, 45, 60));

                g2.fillRoundRect(
                        b.x,
                        b.y,
                        b.width,
                        b.height,
                        14,
                        14);

                // border
                g2.setColor(
                        new Color(120, 140, 180));

                g2.drawRoundRect(
                        b.x,
                        b.y,
                        b.width,
                        b.height,
                        14,
                        14);

                // text
                g2.setColor(
                        new Color(220, 220, 235));

                int strW = g2.getFontMetrics()
                        .stringWidth(labels[i]);

                int tx = b.x + (b.width - strW) / 2;

                int ty = b.y
                        + (b.height / 2)
                        + 7;

                g2.drawString(labels[i], tx, ty);
            }

            // =====================================================
            // ABOUT BUTTON
            // =====================================================

            g2.setColor(
                    new Color(70, 80, 100));

            g2.fillRoundRect(
                    aboutButton.x,
                    aboutButton.y,
                    aboutButton.width,
                    aboutButton.height,
                    10,
                    10);

            g2.setColor(
                    new Color(230, 230, 240));

            g2.setFont(
                    new Font("SansSerif", Font.BOLD, 18));

            g2.drawString(
                    "i",
                    aboutButton.x + 13,
                    aboutButton.y + 24);

            // =====================================================
            // ABOUT PANEL
            // =====================================================

            if (aboutOpen) {

                g2.setColor(
                        new Color(20, 24, 30, 240));

                g2.fillRoundRect(
                        aboutSidebar.x,
                        aboutSidebar.y,
                        aboutSidebar.width,
                        aboutSidebar.height,
                        18,
                        18);

                g2.setColor(
                        new Color(230, 230, 240));

                Font header = new Font("Ariel", Font.BOLD, 16);

                g2.setFont(header);

                int x = aboutSidebar.x + 16;
                int y = aboutSidebar.y + 28;

                g2.drawString(
                        "Thông tin dev:",
                        x,
                        y);

                Font small = new Font("SansSerif", Font.PLAIN, 12);

                g2.setFont(small);

                int sy = y + 24;

                g2.drawString(
                        "Dev: Nhóm 23",
                        x,
                        sy);

                g2.drawString(
                        "WASD để di chuyển",
                        x,
                        sy + 20);

                g2.drawString(
                        "Chuột trái để bắn đạn thường",
                        x,
                        sy + 40);

                g2.drawString(
                        "Chuột phải để shotgun",
                        x,
                        sy + 60);

                g2.drawString(
                        "Shift để Dash (lướt + né đòn)",
                        x,
                        sy + 80);
            }
        }
    }

    private void renderSettings(Graphics2D g2) {
        g2.setColor(new Color(20, 24, 30, 245));
        g2.fillRoundRect(
                settingsPanel.x,
                settingsPanel.y,
                settingsPanel.width,
                settingsPanel.height,
                18,
                18);

        g2.setColor(new Color(120, 140, 180));
        g2.drawRoundRect(
                settingsPanel.x,
                settingsPanel.y,
                settingsPanel.width,
                settingsPanel.height,
                18,
                18);

        g2.setColor(new Color(230, 230, 240));
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.drawString("Settings", settingsPanel.x + 34, settingsPanel.y + 42);

        drawVolumeRow(g2, "Music", musicSliderTrack, musicMuteButton, panel.getMusicVolume());
        drawVolumeRow(g2, "SFX", sfxSliderTrack, sfxMuteButton, panel.getSfxVolume());
        drawButton(g2, settingsBackButton, "Back");
    }

    private void drawVolumeRow(Graphics2D g2, String label, Rectangle track, Rectangle muteButton, float value) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.setColor(new Color(230, 230, 240));
        g2.drawString(label, track.x, track.y - 18);

        int percent = Math.round(value * 100);
        String percentText = percent + "%";
        int percentW = g2.getFontMetrics().stringWidth(percentText);
        g2.drawString(percentText, track.x + track.width - percentW, track.y - 18);

        g2.setColor(new Color(56, 62, 78));
        g2.fillRoundRect(track.x, track.y, track.width, track.height, 8, 8);

        int fillWidth = Math.round(track.width * value);
        g2.setColor(new Color(115, 178, 255));
        g2.fillRoundRect(track.x, track.y, fillWidth, track.height, 8, 8);

        int knobSize = 18;
        int knobX = track.x + fillWidth - knobSize / 2;
        int knobY = track.y + track.height / 2 - knobSize / 2;

        g2.setColor(new Color(235, 238, 245));
        g2.fillOval(knobX, knobY, knobSize, knobSize);
        g2.setColor(new Color(95, 110, 145));
        g2.drawOval(knobX, knobY, knobSize, knobSize);

        drawButton(g2, muteButton, value <= 0.01f ? "Unmute" : "Mute");
    }

    private void drawButton(Graphics2D g2, Rectangle button, String label) {
        g2.setColor(new Color(40, 45, 60));
        g2.fillRoundRect(
                button.x,
                button.y,
                button.width,
                button.height,
                12,
                12);

        g2.setColor(new Color(120, 140, 180));
        g2.drawRoundRect(
                button.x,
                button.y,
                button.width,
                button.height,
                12,
                12);

        g2.setColor(new Color(230, 230, 240));
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));

        int strW = g2.getFontMetrics().stringWidth(label);
        int tx = button.x + (button.width - strW) / 2;
        int ty = button.y + (button.height / 2) + 5;

        g2.drawString(label, tx, ty);
    }
}
