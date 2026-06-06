package goldenroad.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class SaveFeedback {
    private boolean saving;

    public void startSaving() {
        saving = true;
    }

    public void finishSaving() {
        saving = false;
    }

    public boolean isSaving() {
        return saving;
    }

    public String getSavingLabel(long frameCount) {
        int phase = (int) ((frameCount / 15) % 4);
        return switch (phase) {
            case 0 -> "Saving";
            case 1 -> "Saving.";
            case 2 -> "Saving..";
            default -> "Saving...";
        };
    }

    public void render(Graphics2D g2, String label) {
        if (label == null || label.isBlank()) {
            return;
        }

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setComposite(AlphaComposite.SrcOver);

        int panelWidth = UiTheme.BASE_W;
        int panelHeight = UiTheme.BASE_H;
        int boxWidth = 220;
        int boxHeight = 44;
        int x = (panelWidth - boxWidth) / 2;
        int y = panelHeight - 88;

        g2.setColor(new Color(0, 0, 0, 210));
        g2.fillRoundRect(x, y, boxWidth, boxHeight, 14, 14);

        g2.setColor(new Color(120, 180, 255));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        int textWidth = g2.getFontMetrics().stringWidth(label);
        g2.drawString(label, x + (boxWidth - textWidth) / 2, y + 28);
    }
}
