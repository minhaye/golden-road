package goldenroad.ui;

import goldenroad.game.GamePanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class EndScreenOverlay {
    private final Rectangle gameOverRestartButton = new Rectangle((GamePanel.SCREEN_WIDTH / 2) - 110, 170, 220, 44);
    private final Rectangle gameOverReturnButton = new Rectangle((GamePanel.SCREEN_WIDTH / 2) - 110, 220, 220, 44);
    private final Rectangle gameOverExitButton = new Rectangle((GamePanel.SCREEN_WIDTH / 2) - 110, 270, 220, 44);
    private final Rectangle victoryReturnButton = new Rectangle((GamePanel.SCREEN_WIDTH / 2) - 110, 220, 220, 44);
    private final Rectangle victoryExitButton = new Rectangle((GamePanel.SCREEN_WIDTH / 2) - 110, 280, 220, 44);

    public EndScreenAction gameOverActionAt(int x, int y) {
        if (gameOverRestartButton.contains(x, y)) {
            return EndScreenAction.RESTART;
        }
        if (gameOverReturnButton.contains(x, y)) {
            return EndScreenAction.RETURN_TO_MENU;
        }
        if (gameOverExitButton.contains(x, y)) {
            return EndScreenAction.EXIT;
        }
        return EndScreenAction.NONE;
    }

    public EndScreenAction victoryActionAt(int x, int y) {
        if (victoryReturnButton.contains(x, y)) {
            return EndScreenAction.RETURN_TO_MENU;
        }
        if (victoryExitButton.contains(x, y)) {
            return EndScreenAction.EXIT;
        }
        return EndScreenAction.NONE;
    }

    public void renderGameOver(Graphics2D g) {
        g.setColor(new Color(8, 10, 12, 220));
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        g.setFont(new Font("SansSerif", Font.BOLD, 48));
        g.setColor(new Color(100, 228, 250));
        String title = "CONSTRUCT DOWN";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (GamePanel.SCREEN_WIDTH - tw) / 2, 140);

        drawButton(g, gameOverRestartButton, "Restart");
        drawButton(g, gameOverReturnButton, "Return to Menu");
        drawButton(g, gameOverExitButton, "Exit Game");
    }

    public void renderVictory(Graphics2D g) {
        g.setColor(new Color(8, 10, 12, 220));
        g.fillRect(0, 0, GamePanel.SCREEN_WIDTH, GamePanel.SCREEN_HEIGHT);

        g.setFont(new Font("SansSerif", Font.BOLD, 52));
        g.setColor(new Color(190, 240, 160));
        String title = "YOU WIN!";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (GamePanel.SCREEN_WIDTH - tw) / 2, 140);

        g.setFont(new Font("SansSerif", Font.PLAIN, 20));
        g.setColor(new Color(220, 230, 240));
        String message = "Congratulation! Ban da hoan thanh man cuoi cung.";
        int mw = g.getFontMetrics().stringWidth(message);
        g.drawString(message, (GamePanel.SCREEN_WIDTH - mw) / 2, 190);

        drawButton(g, victoryReturnButton, "Return to Menu");
        drawButton(g, victoryExitButton, "Exit Game");
    }

    private void drawButton(Graphics2D g, Rectangle button, String label) {
        g.setColor(new Color(40, 45, 60));
        g.fillRoundRect(button.x, button.y, button.width, button.height, 8, 8);
        g.setColor(new Color(185, 210, 255));
        g.drawRoundRect(button.x, button.y, button.width, button.height, 8, 8);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        int lw = g.getFontMetrics().stringWidth(label);
        int lx = button.x + (button.width - lw) / 2;
        int ly = button.y + (button.height / 2) + 7;
        g.setColor(new Color(230, 230, 240));
        g.drawString(label, lx, ly);
    }

    public enum EndScreenAction {
        NONE,
        RESTART,
        RETURN_TO_MENU,
        EXIT
    }
}
