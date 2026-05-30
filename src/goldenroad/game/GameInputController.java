package goldenroad.game;

import goldenroad.entity.item.Inventory;
import goldenroad.entity.item.Item;
import goldenroad.entity.player.Player;
import goldenroad.input.KeyHandler;
import goldenroad.input.MouseHandler;
import goldenroad.scene.Menu;
import goldenroad.scene.SceneManager;
import goldenroad.ui.Hud;
import goldenroad.ui.InventoryPanel;
import goldenroad.ui.UiTheme;

public class GameInputController {
    private final KeyHandler keyHandler;
    private final MouseHandler mouseHandler;
    private final SceneManager sceneManager;
    private final Menu menu;
    private final Inventory inventory;
    private final InventoryPanel inventoryPanel;

    public GameInputController(
        KeyHandler keyHandler,
        MouseHandler mouseHandler,
        SceneManager sceneManager,
        Menu menu,
        Inventory inventory,
        InventoryPanel inventoryPanel
    ) {
        this.keyHandler = keyHandler;
        this.mouseHandler = mouseHandler;
        this.sceneManager = sceneManager;
        this.menu = menu;
        this.inventory = inventory;
        this.inventoryPanel = inventoryPanel;
    }

    public boolean update(GamePanel panel, Player player) {
        if (keyHandler.consumeMinimapToggleJustPressed()) {
            panel.toggleMinimap();
        }

        if (keyHandler.consumeMapSwitchJustPressed()) {
            panel.switchMap();
            return true;
        }

        if (menu.isActive()) {
            menu.update(mouseHandler);
            return true;
        }

        if (menu.isPaused()) {
            menu.update(mouseHandler);
            if (keyHandler.consumeEscapeJustPressed()) {
                menu.setPaused(false);
                panel.requestFocusInWindow();
            }
            return true;
        }

        menu.update(mouseHandler);

        handleHudMouseInput(panel);

        if (keyHandler.consumeInventoryJustPressed()) {
            inventoryPanel.toggle();
            panel.requestFocusInWindow();
        }

        if (inventoryPanel.isOpen()) {
            inventoryPanel.update(keyHandler, mouseHandler, panel);
            return true;
        }

        if (keyHandler.consumeEscapeJustPressed()) {
            menu.setPaused(true);
            inventoryPanel.close();
            panel.requestFocusInWindow();
            return true;
        }

        if (consumeQuickUse(0, player, panel)) {
            return false;
        }
        consumeQuickUse(1, player, panel);
        consumeQuickUse(2, player, panel);
        return false;
    }

    private boolean consumeQuickUse(int index, Player player, GamePanel panel) {
        if (!keyHandler.consumeQuickUseJustPressed(index)) {
            return false;
        }

        Item.ItemType type = switch (index) {
            case 0 -> Item.ItemType.HP_POTION;
            case 1 -> Item.ItemType.MP_POTION;
            default -> Item.ItemType.KEY;
        };

        if (inventory.useItem(type, player)) {
            panel.showToast("Ban da dung " + getShortLabel(type));
        }
        return true;
    }

    private void handleHudMouseInput(GamePanel panel) {
        if (!mouseHandler.isLeftJustPressed()) {
            return;
        }

        int[] coords = UiTheme.screenToBuffer(
            mouseHandler.getMouseX(),
            mouseHandler.getMouseY(),
            panel.getWidth(),
            panel.getHeight()
        );

        if (!Hud.containsBagButton(coords[0], coords[1])) {
            return;
        }

        mouseHandler.consumeLeftJustPressed();
        inventoryPanel.toggle();
        panel.requestFocusInWindow();
    }

    private String getShortLabel(Item.ItemType type) {
        return switch (type) {
            case HP_POTION -> "HP";
            case MP_POTION -> "MP";
            case KEY -> "Key";
        };
    }
}