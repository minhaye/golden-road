package goldenroad.input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;

public class MouseHandler implements MouseListener, MouseMotionListener {
    private int mouseX;
    private int mouseY;

    private boolean leftClickQueued;
    private boolean rightClickQueued;

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public boolean consumeLeftClick() {
        if (leftClickQueued) {
            leftClickQueued = false;
            return true;
        }
        return false;
    }

    public boolean consumeRightClick() {
        if (rightClickQueued) {
            rightClickQueued = false;
            return true;
        }
        return false;
    }

    public boolean isLeftClickQueued() {
        return leftClickQueued;
    }

    public boolean isRightClickQueued() {
        return rightClickQueued;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        updateMousePosition(e);

        if (SwingUtilities.isLeftMouseButton(e)) {
            leftClickQueued = true;
        }

        if (SwingUtilities.isRightMouseButton(e)) {
            rightClickQueued = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        updateMousePosition(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        updateMousePosition(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateMousePosition(e);
    }

    private void updateMousePosition(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
}
