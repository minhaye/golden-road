package goldenroad.input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.SwingUtilities;

public class MouseHandler implements MouseListener, MouseMotionListener {
    private int mouseX;
    private int mouseY;
    private boolean leftJustPressed;
    private boolean leftPressed;
    private boolean rightPressed;

    

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public boolean isLeftPressed() {
        return leftPressed;
    }

    public boolean isRightPressed() {
        return rightPressed;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

        updateMousePosition(e);

        if (SwingUtilities.isLeftMouseButton(e)) {

           if (!leftPressed) {
                leftJustPressed = true;
            }

            leftPressed = true;
        }

        if (SwingUtilities.isRightMouseButton(e)) {
            rightPressed = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {

        updateMousePosition(e);

        if (SwingUtilities.isLeftMouseButton(e)) {
            leftPressed = false;
        }

        if (SwingUtilities.isRightMouseButton(e)) {
            rightPressed = false;
        }
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

    public boolean consumeLeftJustPressed() {

        if (leftJustPressed) {
            leftJustPressed = false;
            return true;
        }
        return false;
    }

    public boolean isLeftJustPressed() {
        return leftJustPressed;
    }

    private void updateMousePosition(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
}
