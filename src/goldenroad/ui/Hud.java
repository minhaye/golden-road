package goldenroad.ui;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import goldenroad.entity.Inventory;
import goldenroad.entity.Player;
import goldenroad.game.GamePanel;

public class Hud {

    private static final int PADDING = 8;
    private static final int BAR_W = 120;
    private static final int BAR_H = 10;
    private static final int BAR_GAP = 6;
    private static final int BAR_ARC = 6;
    private static final int SKILL_SIZE = 32;
    private static final int SKILL_GAP = 6;
    private static final int BAG_SIZE = 28;

    private final GamePanel panel;
    private final Player player;
    private final Inventory inventory;

    public Hud(GamePanel panel, Player player, Inventory inventory) {
        this.panel = panel;
        this.player = player;
        this.inventory = inventory;
    }

    public void render(Graphics2D g2) {
        UiTheme.enableTextAntialiasing(g2);

        renderResourceBar(g2, PADDING, PADDING, "HP",
            player.getHp(), player.getMaxHp(), UiTheme.HP_FILL);
        renderResourceBar(g2, PADDING, PADDING + BAR_H + BAR_GAP, "MP",
            player.getMp(), player.getMaxMp(), UiTheme.MP_FILL);

        int skillY = UiTheme.BASE_H - PADDING - SKILL_SIZE;
        int skillX = PADDING;

        skillX = renderSkillSlot(g2, skillX, skillY, UiTheme.SKILL_GUN, "LMB",
            panel.getLeftShootCooldown(), panel.getLeftShootCooldownMax());
        skillX = renderSkillSlot(g2, skillX, skillY, UiTheme.SKILL_SHOTGUN, "RMB",
            panel.getRightShootCooldown(), panel.getRightShootCooldownMax());
        skillX = renderSkillSlot(g2, skillX, skillY, UiTheme.SKILL_DASH, "Sh",
            player.getDashCooldown(), player.getDashCooldownMax());
        renderLockedSkillSlot(g2, skillX, skillY, "Lz");

        renderBagButton(g2);
    }

    private void renderResourceBar(Graphics2D g2, int x, int y, String label,
                                   int current, int max, java.awt.Color fill) {
        g2.setFont(UiTheme.FONT_HUD);
        g2.setColor(UiTheme.TEXT);
        g2.drawString(label, x, y + BAR_H - 1);

        int barX = x + 24;
        float ratio = max > 0 ? (float) current / max : 0f;
        UiTheme.drawBar(g2, barX, y, BAR_W, BAR_H, BAR_ARC, fill, ratio);

        String valueText = current + "/" + max;
        g2.setColor(UiTheme.TEXT);
        g2.drawString(valueText, barX + BAR_W + 6, y + BAR_H - 1);
    }

    private int renderSkillSlot(Graphics2D g2, int x, int y, java.awt.Color color,
                                String hint, int cooldown, int maxCooldown) {
        g2.setColor(UiTheme.BUTTON_BG);
        g2.fillRoundRect(x, y, SKILL_SIZE, SKILL_SIZE, 8, 8);
        g2.setColor(color);
        g2.fillRoundRect(x + 4, y + 4, SKILL_SIZE - 8, SKILL_SIZE - 8, 6, 6);
        g2.setColor(UiTheme.ACCENT);
        g2.drawRoundRect(x, y, SKILL_SIZE, SKILL_SIZE, 8, 8);

        if (cooldown > 0 && maxCooldown > 0) {
            float ratio = (float) cooldown / maxCooldown;
            int overlayH = Math.round(SKILL_SIZE * ratio);
            g2.setColor(UiTheme.COOLDOWN_OVERLAY);
            g2.fillRoundRect(x, y, SKILL_SIZE, overlayH, 8, 8);

            g2.setFont(UiTheme.FONT_SKILL);
            g2.setColor(UiTheme.TEXT);
            String seconds = String.format("%.1f", cooldown / 60.0);
            int textW = g2.getFontMetrics().stringWidth(seconds);
            g2.drawString(seconds, x + (SKILL_SIZE - textW) / 2, y + SKILL_SIZE / 2 + 4);
        }

        g2.setFont(UiTheme.FONT_SKILL);
        g2.setColor(UiTheme.TEXT_DIM);
        g2.drawString(hint, x + 2, y + SKILL_SIZE - 2);

        return x + SKILL_SIZE + SKILL_GAP;
    }

    private void renderLockedSkillSlot(Graphics2D g2, int x, int y, String hint) {
        g2.setColor(UiTheme.BUTTON_BG);
        g2.fillRoundRect(x, y, SKILL_SIZE, SKILL_SIZE, 8, 8);
        g2.setColor(UiTheme.SKILL_LOCKED);
        g2.fillRoundRect(x + 4, y + 4, SKILL_SIZE - 8, SKILL_SIZE - 8, 6, 6);
        g2.setColor(UiTheme.ACCENT);
        g2.drawRoundRect(x, y, SKILL_SIZE, SKILL_SIZE, 8, 8);

        g2.setFont(UiTheme.FONT_SKILL);
        g2.setColor(UiTheme.TEXT_DIM);
        int lockW = g2.getFontMetrics().stringWidth("?");
        g2.drawString("?", x + (SKILL_SIZE - lockW) / 2, y + SKILL_SIZE / 2 + 3);
        g2.drawString(hint, x + 2, y + SKILL_SIZE - 2);
    }

    private void renderBagButton(Graphics2D g2) {
        int x = UiTheme.BASE_W - PADDING - BAG_SIZE;
        int y = UiTheme.BASE_H - PADDING - BAG_SIZE;

        g2.setColor(UiTheme.BUTTON_BG);
        g2.fillRoundRect(x, y, BAG_SIZE, BAG_SIZE, 8, 8);
        g2.setColor(UiTheme.ITEM_KEY);
        g2.fillRoundRect(x + 5, y + 6, BAG_SIZE - 10, BAG_SIZE - 12, 4, 4);
        g2.setColor(UiTheme.ACCENT);
        g2.drawRoundRect(x, y, BAG_SIZE, BAG_SIZE, 8, 8);

        int total = inventory.getTotalCount();
        if (total > 0) {
            g2.setFont(UiTheme.FONT_SKILL);
            g2.setColor(UiTheme.TEXT);
            String count = String.valueOf(total);
            int textW = g2.getFontMetrics().stringWidth(count);
            g2.drawString(count, x + BAG_SIZE - textW - 2, y + 10);
        }

        g2.setFont(UiTheme.FONT_HUD_SMALL);
        g2.setColor(UiTheme.TEXT_DIM);
        g2.drawString("Tab", x + 4, y + BAG_SIZE + 10);
    }

    public static Rectangle getBagButtonBounds() {
        int x = UiTheme.BASE_W - PADDING - BAG_SIZE;
        int y = UiTheme.BASE_H - PADDING - BAG_SIZE;
        return new Rectangle(x, y, BAG_SIZE, BAG_SIZE);
    }

    public static boolean containsBagButton(int bufferX, int bufferY) {
        return getBagButtonBounds().contains(bufferX, bufferY);
    }
}
