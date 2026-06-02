package goldenroad.scene;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import goldenroad.game.GamePanel;
import goldenroad.input.MouseHandler;
import goldenroad.settings.Difficulty;
import goldenroad.settings.GameSettings;

public class Menu {
    private enum View {
        BUTTONS,
        SETTINGS,
        TUTORIAL
    }

    private static final int BASE_W = GamePanel.SCREEN_WIDTH;
    private static final int BASE_H = GamePanel.SCREEN_HEIGHT;

    private final GamePanel panel;
    private final GameSettings settings;

    private boolean active = true;
    private boolean paused = false;
    private boolean aboutOpen = false;
    private View view = View.BUTTONS;

    private Rectangle[] buttons;
    private Rectangle[] pauseButtons;
    private Rectangle[] difficultyButtons;
    private Rectangle volumeMinusButton;
    private Rectangle volumePlusButton;
    private Rectangle backButton;
    private Rectangle aboutButton;
    private Rectangle aboutSidebar;

    private final String[] labels = {
        "Start",
        "Continue",
        "Settings",
        "Tutorial",
        "Exit"
    };

    private final String[] pauseLabels = {
        "Continue",
        "Restart",
        "Settings",
        "Tutorial",
        "Menu",
        "Exit"
    };

    public Menu(GamePanel panel, GameSettings settings) {
        this.panel = panel;
        this.settings = settings;
        layoutElements();
        layoutPauseElements();
        layoutSubmenuElements();
    }

    private void layoutElements() {
        int btnW = 220;
        int btnH = 44;
        int gap = 14;

        int totalH = (btnH * labels.length) + (gap * (labels.length - 1));
        int startY = (BASE_H / 2) - (totalH / 2);
        int centerX = BASE_W / 2;

        buttons = new Rectangle[labels.length];
        for (int i = 0; i < labels.length; i++) {
            buttons[i] = new Rectangle(centerX - (btnW / 2), startY + i * (btnH + gap), btnW, btnH);
        }

        int aboutSize = 36;
        aboutButton = new Rectangle(BASE_W - aboutSize - 12, BASE_H - aboutSize - 12, aboutSize, aboutSize);
        aboutSidebar = new Rectangle(BASE_W - 320, 40, 300, BASE_H - 80);
    }

    private void layoutPauseElements() {
        int btnW = 220;
        int btnH = 36;
        int gap = 10;

        int totalH = (btnH * pauseLabels.length) + (gap * (pauseLabels.length - 1));
        int startY = (BASE_H / 2) - (totalH / 2) + 44;
        int centerX = BASE_W / 2;

        pauseButtons = new Rectangle[pauseLabels.length];
        for (int i = 0; i < pauseLabels.length; i++) {
            pauseButtons[i] = new Rectangle(centerX - (btnW / 2), startY + i * (btnH + gap), btnW, btnH);
        }
    }

    private void layoutSubmenuElements() {
        Difficulty[] difficulties = Difficulty.values();
        difficultyButtons = new Rectangle[difficulties.length];

        int diffW = 120;
        int diffH = 38;
        int diffGap = 14;
        int totalDiffW = diffW * difficulties.length + diffGap * (difficulties.length - 1);
        int startX = (BASE_W - totalDiffW) / 2;

        for (int i = 0; i < difficulties.length; i++) {
            difficultyButtons[i] = new Rectangle(startX + i * (diffW + diffGap), 218, diffW, diffH);
        }

        volumeMinusButton = new Rectangle((BASE_W / 2) - 104, 142, 44, 38);
        volumePlusButton = new Rectangle((BASE_W / 2) + 60, 142, 44, 38);
        backButton = new Rectangle((BASE_W / 2) - 90, 338, 180, 42);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
        if (paused) {
            active = false;
        }
        resetSubmenuState();
    }

    public void update(MouseHandler mouse) {
        if (!active && !paused) {
            aboutOpen = false;
            return;
        }

        int[] mousePoint = toMenuPoint(mouse);
        int mx = mousePoint[0];
        int my = mousePoint[1];

        if (!mouse.isLeftJustPressed()) {
            return;
        }

        if (!mouse.consumeLeftJustPressed()) {
            return;
        }

        if (view == View.BUTTONS && aboutButton.contains(mx, my)) {
            aboutOpen = !aboutOpen;
            return;
        }

        if (aboutOpen && !aboutSidebar.contains(mx, my)) {
            aboutOpen = false;
        }

        if (view == View.SETTINGS) {
            handleSettingsClick(mx, my);
            return;
        }

        if (view == View.TUTORIAL) {
            if (backButton.contains(mx, my)) {
                resetSubmenuState();
            }
            return;
        }

        if (paused) {
            for (int i = 0; i < pauseButtons.length; i++) {
                if (pauseButtons[i].contains(mx, my)) {
                    handlePauseButton(i);
                    return;
                }
            }
            return;
        }

        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].contains(mx, my)) {
                handleButton(i);
                return;
            }
        }
    }

    private int[] toMenuPoint(MouseHandler mouse) {
        double scaleX = (double) panel.getWidth() / BASE_W;
        double scaleY = (double) panel.getHeight() / BASE_H;
        double scale = Math.min(scaleX, scaleY);

        if (scale <= 0) {
            return new int[] { -1, -1 };
        }

        int renderWidth = (int) (BASE_W * scale);
        int renderHeight = (int) (BASE_H * scale);
        int offsetX = (panel.getWidth() - renderWidth) / 2;
        int offsetY = (panel.getHeight() - renderHeight) / 2;

        int mx = (int) ((mouse.getMouseX() - offsetX) / scale);
        int my = (int) ((mouse.getMouseY() - offsetY) / scale);
        return new int[] { mx, my };
    }

    private void handleButton(int index) {
        switch (index) {
            case 0:
                panel.startNewGame();
                closeMenu();
                break;
            case 1:
                panel.continueGame();
                closeMenu();
                break;
            case 2:
                view = View.SETTINGS;
                aboutOpen = false;
                break;
            case 3:
                view = View.TUTORIAL;
                aboutOpen = false;
                break;
            case 4:
                System.exit(0);
                break;
            default:
                break;
        }
    }

    private void handlePauseButton(int index) {
        switch (index) {
            case 0:
                paused = false;
                resetSubmenuState();
                panel.requestFocusInWindow();
                break;
            case 1:
                panel.restartCurrentMap();
                paused = false;
                resetSubmenuState();
                break;
            case 2:
                view = View.SETTINGS;
                aboutOpen = false;
                break;
            case 3:
                view = View.TUTORIAL;
                aboutOpen = false;
                break;
            case 4:
                panel.menu.open();
                paused = false;
                resetSubmenuState();
                break;
            case 5:
                System.exit(0);
                break;
            default:
                break;
        }
    }

    private void handleSettingsClick(int mx, int my) {
        if (volumeMinusButton.contains(mx, my)) {
            settings.setVolume(settings.getVolume() - 10);
            panel.saveSettings();
            return;
        }

        if (volumePlusButton.contains(mx, my)) {
            settings.setVolume(settings.getVolume() + 10);
            panel.saveSettings();
            return;
        }

        Difficulty[] difficulties = Difficulty.values();
        for (int i = 0; i < difficulties.length; i++) {
            if (difficultyButtons[i].contains(mx, my)) {
                settings.setDifficulty(difficulties[i]);
                panel.saveSettings();
                return;
            }
        }

        if (backButton.contains(mx, my)) {
            resetSubmenuState();
        }
    }

    private void closeMenu() {
        active = false;
        paused = false;
        resetSubmenuState();
        panel.requestFocusInWindow();
    }

    private void resetSubmenuState() {
        view = View.BUTTONS;
        aboutOpen = false;
    }

    public void render(Graphics2D g2) {
        g2.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        );

        if (active) {
            renderMenuBackground(g2, 220);
        } else if (paused) {
            renderMenuBackground(g2, 200);
            if (view == View.BUTTONS) {
                renderPauseTitle(g2);
            }
        }

        if (!active && !paused) {
            return;
        }

        if (view == View.SETTINGS) {
            renderSettings(g2);
        } else if (view == View.TUTORIAL) {
            renderTutorial(g2);
        } else if (paused) {
            renderPauseButtonRow(g2);
            renderAboutButton(g2);
        } else {
            renderButtonRow(g2, buttons, labels);
            renderAboutButton(g2);
        }

        if (aboutOpen && view == View.BUTTONS) {
            renderAboutPanel(g2);
        }
    }

    public void open() {
        active = true;
        paused = false;
        resetSubmenuState();
        panel.requestFocusInWindow();
    }

    private void renderMenuBackground(Graphics2D g2, int alpha) {
        g2.setColor(new Color(8, 10, 12, alpha));
        g2.fillRect(0, 0, BASE_W, BASE_H);
    }

    private void renderPauseTitle(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 22));
        g2.setColor(new Color(220, 220, 235));
        drawCentered(g2, "Paused", 118);
    }

    private void renderButtonRow(Graphics2D g2, Rectangle[] row, String[] rowLabels) {
        Font font = new Font("SansSerif", Font.BOLD, 20);
        g2.setFont(font);

        for (int i = 0; i < row.length; i++) {
            renderButton(g2, row[i], rowLabels[i], false);
        }
    }

    private void renderPauseButtonRow(Graphics2D g2) {
        Font font = new Font("SansSerif", Font.BOLD, 16);
        g2.setFont(font);

        for (int i = 0; i < pauseButtons.length; i++) {
            renderButton(g2, pauseButtons[i], pauseLabels[i], false);
        }
    }

    private void renderSettings(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2.setColor(new Color(230, 230, 240));
        drawCentered(g2, "Settings", 90);

        g2.setFont(new Font("SansSerif", Font.BOLD, 17));
        drawCentered(g2, "Volume", 130);

        renderButton(g2, volumeMinusButton, "-", false);
        renderButton(g2, volumePlusButton, "+", false);

        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        drawCentered(g2, settings.getVolume() + "%", 168);

        g2.setFont(new Font("SansSerif", Font.BOLD, 17));
        drawCentered(g2, "Difficulty", 206);

        Difficulty[] difficulties = Difficulty.values();
        for (int i = 0; i < difficulties.length; i++) {
            boolean selected = settings.getDifficulty() == difficulties[i];
            renderButton(g2, difficultyButtons[i], difficulties[i].getDisplayName(), selected);
        }

        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g2.setColor(new Color(180, 190, 210));
        drawCentered(g2, difficultyHint(settings.getDifficulty()), 292);

        renderButton(g2, backButton, "Back", false);
    }

    private String difficultyHint(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "Monster cham hon, item HP/MP xuat hien nhieu hon.";
            case HARD -> "Monster nhanh va danh mau hon, HP/MP item it hon.";
            case NORMAL -> "Can bang toc do monster va item.";
        };
    }

    private void renderTutorial(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2.setColor(new Color(230, 230, 240));
        drawCentered(g2, "Tutorial", 76);

        String[] lines = {
            "Goal: clear monsters, collect Key, move to the next map.",
            "Move with A/D or arrow keys. Jump with W, Up, or Space.",
            "Use Shift to dash. Aim and shoot with the mouse.",
            "Walk into HP, MP, or Key items to pick them up.",
            "Open inventory with Tab or I, use quick items with 1/2/3.",
            "Esc opens this menu while playing.",
            "Test controls stay enabled: Alt+M changes map, Alt+X kills monsters."
        };

        g2.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g2.setColor(new Color(220, 224, 236));
        int x = 120;
        int y = 122;
        for (String line : lines) {
            g2.drawString(line, x, y);
            y += 28;
        }

        renderButton(g2, backButton, "Back", false);
    }

    private void renderButton(Graphics2D g2, Rectangle b, String label, boolean selected) {
        g2.setColor(selected ? new Color(70, 95, 145) : new Color(40, 45, 60));
        g2.fillRoundRect(b.x, b.y, b.width, b.height, 8, 8);

        g2.setColor(selected ? new Color(185, 210, 255) : new Color(120, 140, 180));
        g2.drawRoundRect(b.x, b.y, b.width, b.height, 8, 8);

        g2.setColor(new Color(230, 230, 240));
        Font currentFont = g2.getFont();
        if (currentFont.getSize() > 20) {
            g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        }
        int strW = g2.getFontMetrics().stringWidth(label);
        int tx = b.x + (b.width - strW) / 2;
        int ty = b.y + (b.height / 2) + 7;
        g2.drawString(label, tx, ty);
        g2.setFont(currentFont);
    }

    private void renderAboutButton(Graphics2D g2) {
        g2.setColor(new Color(70, 80, 100));
        g2.fillRoundRect(aboutButton.x, aboutButton.y, aboutButton.width, aboutButton.height, 8, 8);

        g2.setColor(new Color(230, 230, 240));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("i", aboutButton.x + 15, aboutButton.y + 24);
    }

    private void renderAboutPanel(Graphics2D g2) {
        g2.setColor(new Color(20, 24, 30, 240));
        g2.fillRoundRect(aboutSidebar.x, aboutSidebar.y, aboutSidebar.width, aboutSidebar.height, 8, 8);

        int x = aboutSidebar.x + 16;
        int y = aboutSidebar.y + 30;

        g2.setColor(new Color(230, 230, 240));
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("Developer info", x, y);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        int sy = y + 28;
        g2.drawString("Dev: Nhom 23", x, sy);
        g2.drawString("Golden Road", x, sy + 22);
        g2.drawString("Esc: pause / close menu", x, sy + 44);
        g2.drawString("Alt+M: switch map for testing", x, sy + 66);
        g2.drawString("Alt+X: kill all monsters for testing", x, sy + 88);
    }

    private void drawCentered(Graphics2D g2, String text, int y) {
        int strW = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (BASE_W - strW) / 2, y);
    }
}
