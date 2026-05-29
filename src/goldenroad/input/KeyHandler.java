package goldenroad.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    public boolean leftPressed;
    public boolean rightPressed;
    public boolean jumpPressed;
    public boolean downPressed;
    public boolean sprintPressed;
    public boolean dashPressed;
    private boolean dashJustPressed;
    private boolean jumpJustPressed;
    private boolean inventoryJustPressed;
    private boolean escapeJustPressed;
    private boolean enterJustPressed;
    private boolean mapSwitchJustPressed;
    private boolean mapSwitchArmed;
    private boolean minimapToggleJustPressed;
    private boolean minimapToggleArmed;
    private final boolean[] quickUseJustPressed = new boolean[3];


    public boolean consumeJumpJustPressed() {
        if (jumpJustPressed) {
            jumpJustPressed = false;
            return true;
        }
        return false;
    }

    public boolean consumeDashJustPressed() {
        if (dashJustPressed) {
            dashJustPressed = false;
            return true;
        }
        return false;
    }

    public boolean consumeInventoryJustPressed() {
        if (inventoryJustPressed) {
            inventoryJustPressed = false;
            return true;
        }
        return false;
    }

    public boolean consumeEscapeJustPressed() {
        if (escapeJustPressed) {
            escapeJustPressed = false;
            return true;
        }
        return false;
    }

    public boolean consumeEnterJustPressed() {
        if (enterJustPressed) {
            enterJustPressed = false;
            return true;
        }
        return false;
    }

    public boolean consumeMapSwitchJustPressed() {
        if (mapSwitchJustPressed) {
            mapSwitchJustPressed = false;
            return true;
        }
        return false;
    }

    public boolean consumeMinimapToggleJustPressed() {
        if (minimapToggleJustPressed) {
            minimapToggleJustPressed = false;
            return true;
        }
        return false;
    }

    public boolean consumeQuickUseJustPressed(int index) {
        if (index < 0 || index >= quickUseJustPressed.length) {
            return false;
        }
        if (quickUseJustPressed[index]) {
            quickUseJustPressed[index] = false;
            return true;
        }
        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftPressed = true;
        }

        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightPressed = true;
        }

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP || code == KeyEvent.VK_SPACE) {
            if (!jumpPressed) {
                jumpJustPressed = true;
            }
            jumpPressed = true;
        }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downPressed = true;
        }

        if (code == KeyEvent.VK_CONTROL) {
            sprintPressed = true;
        }
        if (code == KeyEvent.VK_SHIFT) {
            if (!dashPressed) {
                dashJustPressed = true;
            }
            dashPressed = true;
        }

        if (code == KeyEvent.VK_TAB || code == KeyEvent.VK_I) {
            e.consume();
            inventoryJustPressed = true;
        }

        if (code == KeyEvent.VK_ESCAPE) {
            e.consume();
            escapeJustPressed = true;
        }

        if (code == KeyEvent.VK_ENTER) {
            e.consume();
            enterJustPressed = true;
        }

        if (code == KeyEvent.VK_M && e.isAltDown() && !mapSwitchArmed) {
            e.consume();
            mapSwitchJustPressed = true;
            mapSwitchArmed = true;
        }

        if (code == KeyEvent.VK_N && e.isAltDown() && !minimapToggleArmed) {
            e.consume();
            minimapToggleJustPressed = true;
            minimapToggleArmed = true;
        }

        if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_3) {
            quickUseJustPressed[code - KeyEvent.VK_1] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }

        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP || code == KeyEvent.VK_SPACE) {
            jumpPressed = false;
        }

        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downPressed = false;
        }

        if (code == KeyEvent.VK_CONTROL) {
            sprintPressed = false;
        }
        if (code == KeyEvent.VK_SHIFT) {
            dashPressed = false;
        }

        if (code == KeyEvent.VK_ALT || code == KeyEvent.VK_M) {
            mapSwitchArmed = false;
        }

        if (code == KeyEvent.VK_ALT || code == KeyEvent.VK_N) {
            minimapToggleArmed = false;
        }
    }

}
