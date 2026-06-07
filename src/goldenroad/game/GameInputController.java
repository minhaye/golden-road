package goldenroad.game;

import goldenroad.entity.item.Inventory;
import goldenroad.entity.item.Item;
import goldenroad.entity.item.ItemUseResult;
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

        if (keyHandler.consumeKeyMarkerToggleJustPressed()) {
            panel.toggleKeyMarker();
        }

        if (keyHandler.consumeMapSwitchJustPressed()) {
            panel.switchMap();
            return true;
        }

        if (keyHandler.consumeKillAllMonstersJustPressed()) {
            panel.killAllMonstersOnCurrentMap();
            return false;
        }

        if (menu.isActive()) {
            menu.update(mouseHandler);
            mouseHandler.suppressGameplayMouse();
            return true;
        }

        if (menu.isPaused()) {
            menu.update(mouseHandler);
            if (keyHandler.consumeEscapeJustPressed()) {
                menu.setPaused(false);
                panel.requestFocusInWindow();
            }
            mouseHandler.suppressGameplayMouse();
            return true;
        }

        menu.update(mouseHandler);

        if (keyHandler.consumeInventoryJustPressed()) {
            inventoryPanel.toggle();
            panel.requestFocusInWindow();
        }

        boolean inventoryOpen = inventoryPanel.isOpen();
        if (inventoryOpen) {
            inventoryPanel.update(keyHandler, mouseHandler, panel);
            mouseHandler.suppressGameplayMouse();
        } else {
            handleHudMouseInput(panel);

            if (keyHandler.consumeEscapeJustPressed()) {
                menu.setPaused(true);
                inventoryPanel.close();
                panel.playPauseSound();
                panel.requestFocusInWindow();
                return true;
            }
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

        ItemUseResult result = inventory.useItem(type);
        panel.showToast(result.message());
        panel.playItemUseSound(type, result);
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
}
