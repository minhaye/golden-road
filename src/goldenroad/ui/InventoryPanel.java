package goldenroad.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import goldenroad.entity.item.Inventory;
import goldenroad.entity.item.Item;
import goldenroad.entity.item.ItemUseResult;
import goldenroad.entity.player.Player;
import goldenroad.game.GamePanel;
import goldenroad.input.KeyHandler;
import goldenroad.input.MouseHandler;

public class InventoryPanel {

    private static final int PANEL_X = 160;
    private static final int PANEL_Y = 40;
    private static final int PANEL_W = 320;
    private static final int PANEL_H = 280;
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
    private int selectedIndex = -1;

    private final BufferedImage hpItemSprite;
    private final BufferedImage mpItemSprite;
    private final BufferedImage keyItemSprite;

    public InventoryPanel(Inventory inventory, Player player) {
        this.inventory = inventory;
        hpItemSprite = loadSprite("/assets/item/hp.png");
        mpItemSprite = loadSprite("/assets/item/mp.png");
        keyItemSprite = loadSprite("/assets/item/key.png");
    }

    private BufferedImage loadSprite(String resourcePath) {
        try {
            var stream = getClass().getResourceAsStream(resourcePath);
            if (stream == null) {
                System.out.println("Khong tim thay resource: " + resourcePath);
                return null;
            }
            return ImageIO.read(stream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    private List<Item.ItemType> getVisibleTypes() {
        List<Item.ItemType> visible = new ArrayList<>();
        for (Item.ItemType type : SLOT_TYPES) {
            if (inventory.getCount(type) > 0) {
                visible.add(type);
            }
        }
        return visible;
    }

    private int getHotkeyIndex(Item.ItemType type) {
        for (int i = 0; i < SLOT_TYPES.length; i++) {
            if (SLOT_TYPES[i] == type) {
                return i;
            }
        }
        return -1;
    }

    private void normalizeSelection() {
        List<Item.ItemType> visible = getVisibleTypes();
        if (visible.isEmpty()) {
            selectedIndex = -1;
            return;
        }

        if (selectedIndex < 0 || selectedIndex >= visible.size()) {
            selectedIndex = 0;
        }
    }

    private void useItemType(Item.ItemType type, GamePanel panel) {
        if (type == null || inventory.getCount(type) <= 0) {
            return;
        }

        ItemUseResult result = inventory.useItem(type);
        panel.showToast(result.message());
        normalizeSelection();
    }

    private void useSelectedItem(GamePanel panel) {
        List<Item.ItemType> visible = getVisibleTypes();
        if (selectedIndex < 0 || selectedIndex >= visible.size()) {
            return;
        }

        useItemType(visible.get(selectedIndex), panel);
    }

    public void update(KeyHandler keyHandler, MouseHandler mouse, GamePanel panel) {
        if (!open) {
            return;
        }

        if (keyHandler.consumeEscapeJustPressed()) {
            close();
            return;
        }

        List<Item.ItemType> visible = getVisibleTypes();

        if (keyHandler.consumeJumpJustPressed() && !visible.isEmpty()) {
            normalizeSelection();
            selectedIndex = (selectedIndex + 1) % visible.size();
        }

        if (keyHandler.consumeEnterJustPressed()) {
            useSelectedItem(panel);
        }

        for (int i = 0; i < SLOT_TYPES.length; i++) {
            if (keyHandler.consumeQuickUseJustPressed(i)) {
                useItemType(SLOT_TYPES[i], panel);
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

        for (int i = 0; i < visible.size(); i++) {
            Rectangle slot = getSlotBounds(i);
            if (slot.contains(mx, my)) {
                selectedIndex = i;
                useItemType(visible.get(i), panel);
                return;
            }
        }
    }

    public void render(Graphics2D g2) {
        if (!open) {
            return;
        }

        List<Item.ItemType> visible = getVisibleTypes();

        UiTheme.enableTextAntialiasing(g2);
        g2.setComposite(AlphaComposite.SrcOver);

        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, UiTheme.BASE_W, UiTheme.BASE_H);

        UiTheme.drawRoundPanel(g2, PANEL_X, PANEL_Y, PANEL_W, PANEL_H, 18);

        g2.setFont(UiTheme.FONT_TITLE);
        g2.setColor(UiTheme.TEXT);
        g2.drawString("Inventory", PANEL_X + 16, PANEL_Y + 28);

        if (visible.isEmpty()) {
            return;
        }

        normalizeSelection();

        for (int i = 0; i < visible.size(); i++) {
            renderSlot(g2, i, visible.get(i));
        }

        renderDescriptions(g2, visible);
    }

    private void renderSlot(Graphics2D g2, int index, Item.ItemType type) {
        Rectangle slot = getSlotBounds(index);
        int count = inventory.getCount(type);
        boolean selected = index == selectedIndex;
        int hotkey = getHotkeyIndex(type);

        g2.setColor(UiTheme.BUTTON_BG);
        g2.fillRoundRect(slot.x, slot.y, slot.width, slot.height, 10, 10);

        g2.setColor(new Color(40, 44, 56));
        g2.fillRoundRect(slot.x + 6, slot.y + 6, slot.width - 12, slot.height - 12, 8, 8);

        BufferedImage sprite = getItemSprite(type);
        if (sprite != null) {
            g2.drawImage(sprite, slot.x + 8, slot.y + 8, slot.width - 16, slot.height - 16, null);
        } else {
            g2.setColor(getItemColor(type));
            g2.fillRoundRect(slot.x + 10, slot.y + 10, slot.width - 20, slot.height - 20, 8, 8);
        }

        g2.setColor(selected ? UiTheme.HP_FILL : UiTheme.ACCENT);
        g2.drawRoundRect(slot.x, slot.y, slot.width, slot.height, 10, 10);

        g2.setFont(UiTheme.FONT_HUD);
        g2.setColor(UiTheme.TEXT);
        String countText = "x" + count;
        int countW = g2.getFontMetrics().stringWidth(countText);
        g2.drawString(countText, slot.x + slot.width - countW - 4, slot.y + 14);

        g2.setFont(UiTheme.FONT_SKILL);
        g2.setColor(UiTheme.TEXT_DIM);
        g2.drawString(getShortLabel(type), slot.x + 4, slot.y + slot.height - 4);

        if (hotkey >= 0) {
            g2.setFont(UiTheme.FONT_HUD_SMALL);
            g2.setColor(UiTheme.TEXT);
            g2.drawString(String.valueOf(hotkey + 1), slot.x + 4, slot.y + 12);
        }
    }

    private void renderDescriptions(Graphics2D g2, List<Item.ItemType> visible) {
        int descY = GRID_Y + SLOT_SIZE + 24;
        g2.setFont(UiTheme.FONT_BODY);
        g2.setColor(UiTheme.TEXT_DIM);

        for (int i = 0; i < visible.size(); i++) {
            Item.ItemType type = visible.get(i);
            g2.drawString(
                inventory.getDescription(type) + " (x" + inventory.getCount(type) + ")",
                GRID_X,
                descY + i * 16
            );
        }
    }

    private BufferedImage getItemSprite(Item.ItemType type) {
        return switch (type) {
            case HP_POTION -> hpItemSprite;
            case MP_POTION -> mpItemSprite;
            case KEY -> keyItemSprite;
        };
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

    private Rectangle getSlotBounds(int index) {
        int x = GRID_X + index * (SLOT_SIZE + SLOT_GAP);
        int y = GRID_Y;
        return new Rectangle(x, y, SLOT_SIZE, SLOT_SIZE);
    }
}
