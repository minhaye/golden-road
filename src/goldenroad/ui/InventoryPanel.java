package goldenroad.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import goldenroad.entity.Inventory;
import goldenroad.entity.Item;
import goldenroad.entity.Player;
import goldenroad.game.GamePanel;
import goldenroad.input.KeyHandler;
import goldenroad.input.MouseHandler;

public class InventoryPanel {

    private static final int PANEL_X = 160;
    private static final int PANEL_Y = 40;
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 280;
    private static final int COLS = 4;
    private static final int ROWS = 3;
    private static final int SLOT_SIZE = 48;
    private static final int SLOT_GAP = 10;
    private static final int GRID_X = PANEL_X + 20;
    private static final int GRID_Y = PANEL_Y + 48;

    private static final Item.ItemType[] SLOT_TYPES = {
        Item.ItemType.HP_POTION,
        Item.ItemType.MP_POTION,
        Item.ItemType.KEY
    };

    private volatile boolean open = false;
    private final Inventory inventory;
    private final Player player;
    private int selectedIndex = 0;

    public InventoryPanel(Inventory inventory, Player player) {
        this.inventory = inventory;
        this.player = player;
    }

    public boolean isOpen() {
        return open;
    }

    public void toggle() {
        open = !open;
        if (open) {
            normalizeSelection();
        }
    }

    public void close() {
        open = false;
    }

    public int getSelectedSlotIndex() {
        return selectedIndex;
    }

    public void selectSlot(int index) {
        if (index < 0 || index >= SLOT_TYPES.length) {
            return;
        }
        selectedIndex = index;
    }

    private Item.ItemType getSelectedType() {
        if (selectedIndex < 0 || selectedIndex >= SLOT_TYPES.length) {
            return null;
        }
        return SLOT_TYPES[selectedIndex];
    }

    private void normalizeSelection() {
        if (inventory.getTotalCount() <= 0) {
            selectedIndex = -1;
            return;
        }

        if (selectedIndex < 0 || selectedIndex >= SLOT_TYPES.length || inventory.getCount(getSelectedType()) <= 0) {
            selectedIndex = findNextAvailableSlot(0, 1);
        }
    }

    private int findNextAvailableSlot(int startIndex, int direction) {
        int index = startIndex;

        for (int i = 0; i < SLOT_TYPES.length; i++) {
            if (inventory.getCount(SLOT_TYPES[index]) > 0) {
                return index;
            }

            index = (index + direction + SLOT_TYPES.length) % SLOT_TYPES.length;
        }

        return -1;
    }

    private int findNextSelectedIndex(int fromIndex, int direction) {
        if (inventory.getTotalCount() <= 0) {
            return -1;
        }

        int index = fromIndex;
        for (int i = 0; i < SLOT_TYPES.length; i++) {
            index = (index + direction + SLOT_TYPES.length) % SLOT_TYPES.length;
            if (inventory.getCount(SLOT_TYPES[index]) > 0) {
                return index;
            }
        }
        return -1;
    }

    private boolean useSelectedItem(GamePanel panel) {
        if (selectedIndex < 0 || selectedIndex >= SLOT_TYPES.length) {
            return false;
        }

        Item.ItemType type = SLOT_TYPES[selectedIndex];
        boolean used = inventory.useItem(type, player);
        if (!used) {
            return false;
        }

        panel.showToast("Ban da dung " + getShortLabel(type));

        if (inventory.getCount(type) <= 0) {
            selectedIndex = findNextSelectedIndex(selectedIndex, 1);
        }

        return true;
    }

    public void update(KeyHandler keyHandler, MouseHandler mouse, GamePanel panel) {
        if (!open) {
            return;
        }

        if (keyHandler.consumeEscapeJustPressed()) {
            close();
            return;
        }

        if (keyHandler.consumeJumpJustPressed()) {
            selectedIndex = findNextSelectedIndex(selectedIndex < 0 ? 0 : selectedIndex, 1);
        }

        if (keyHandler.consumeEnterJustPressed()) {
            useSelectedItem(panel);
        }

        for (int i = 0; i < SLOT_TYPES.length; i++) {
            if (keyHandler.consumeQuickUseJustPressed(i)) {
                if (inventory.useItem(SLOT_TYPES[i], player)) {
                    panel.showToast("Ban da dung " + getShortLabel(SLOT_TYPES[i]));
                    if (inventory.getCount(SLOT_TYPES[i]) <= 0 && i == selectedIndex) {
                        selectedIndex = findNextSelectedIndex(selectedIndex, 1);
                    }
                }
            }
        }

        if (!mouse.consumeLeftJustPressed()) {
            return;
        }

        int[] bufferCoords = UiTheme.screenToBuffer(
            mouse.getMouseX(),
            mouse.getMouseY(),
            panel.getWidth(),
            panel.getHeight()
        );
        int mx = bufferCoords[0];
        int my = bufferCoords[1];

        for (int i = 0; i < SLOT_TYPES.length; i++) {
            Rectangle slot = getSlotBounds(i);
            if (slot.contains(mx, my)) {
                selectSlot(i);
                return;
            }
        }
    }

    public void render(Graphics2D g2) {
        if (!open) {
            return;
        }

        UiTheme.enableTextAntialiasing(g2);
        g2.setComposite(AlphaComposite.SrcOver);

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRect(0, 0, UiTheme.BASE_W, UiTheme.BASE_H);

        UiTheme.drawRoundPanel(g2, PANEL_X, PANEL_Y, PANEL_W, PANEL_H, 18);

        g2.setFont(UiTheme.FONT_TITLE);
        g2.setColor(UiTheme.TEXT);
        g2.drawString("Inventory", PANEL_X + 16, PANEL_Y + 28);

        for (int i = 0; i < SLOT_TYPES.length; i++) {
            renderSlot(g2, i);
        }
    }

    private void renderSlot(Graphics2D g2, int index) {
        Item.ItemType type = SLOT_TYPES[index];
        Rectangle slot = getSlotBounds(index);
        int count = inventory.getCount(type);
        boolean selected = index == selectedIndex;

        g2.setColor(UiTheme.BUTTON_BG);
        g2.fillRoundRect(slot.x, slot.y, slot.width, slot.height, 10, 10);
        if (count > 0) {
            g2.setColor(getItemColor(type));
            g2.fillRoundRect(slot.x + 6, slot.y + 6, slot.width - 12, slot.height - 12, 8, 8);
        } else {
            g2.setColor(new Color(40, 44, 56));
            g2.fillRoundRect(slot.x + 6, slot.y + 6, slot.width - 12, slot.height - 12, 8, 8);
        }
        g2.setColor(selected ? UiTheme.HP_FILL : UiTheme.ACCENT);
        g2.drawRoundRect(slot.x, slot.y, slot.width, slot.height, 10, 10);

        if (count > 0) {
            g2.setFont(UiTheme.FONT_HUD);
            g2.setColor(UiTheme.TEXT);
            String countText = "x" + count;
            int countW = g2.getFontMetrics().stringWidth(countText);
            g2.drawString(countText, slot.x + slot.width - countW - 4, slot.y + 14);
        }

        g2.setFont(UiTheme.FONT_SKILL);
        g2.setColor(UiTheme.TEXT_DIM);
        g2.drawString(getShortLabel(type), slot.x + 4, slot.y + slot.height - 4);

        g2.setFont(UiTheme.FONT_HUD_SMALL);
        g2.setColor(UiTheme.TEXT);
        g2.drawString(String.valueOf(index + 1), slot.x + 4, slot.y + 12);

        int descY = GRID_Y + ROWS * (SLOT_SIZE + SLOT_GAP) + 8;
        if (index == 0) {
            g2.setFont(UiTheme.FONT_BODY);
            g2.setColor(UiTheme.TEXT_DIM);
            for (int i = 0; i < SLOT_TYPES.length; i++) {
                g2.drawString(
                    inventory.getDescription(SLOT_TYPES[i]) + " (x" + inventory.getCount(SLOT_TYPES[i]) + ")",
                    GRID_X,
                    descY + i * 16
                );
            }
        }
    }

    private Rectangle getSlotBounds(int index) {
        int col = index % COLS;
        int row = index / COLS;
        int x = GRID_X + col * (SLOT_SIZE + SLOT_GAP);
        int y = GRID_Y + row * (SLOT_SIZE + SLOT_GAP);
        return new Rectangle(x, y, SLOT_SIZE, SLOT_SIZE);
    }

    private Color getItemColor(Item.ItemType type) {
        return switch (type) {
            case HP_POTION -> UiTheme.ITEM_HP;
            case MP_POTION -> UiTheme.ITEM_MP;
            case KEY -> UiTheme.ITEM_KEY;
        };
    }

    private String getShortLabel(Item.ItemType type) {
        return switch (type) {
            case HP_POTION -> "HP";
            case MP_POTION -> "MP";
            case KEY -> "Key";
        };
    }
}
