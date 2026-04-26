package goldenroad.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    public boolean leftPressed;
    public boolean rightPressed;
    public boolean jumpPressed;
    public boolean sprintPressed;
    public boolean dashPressed;
    private boolean dashJustPressed;
    private boolean jumpJustPressed;


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

        if (code == KeyEvent.VK_CONTROL) {
            sprintPressed = true;
        }
        if (code == KeyEvent.VK_SHIFT) {
            dashPressed = true;
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

        if (code == KeyEvent.VK_CONTROL) {
            sprintPressed = false;
        }
        if (code == KeyEvent.VK_SHIFT) {
            dashPressed = false;
        }
    }


    /*
    private final int FPS_SET = 120;
    @Override
    public void run() {
        double timePerFrame = 1000000000.0 / FPS_SET;
        long lastFrame = System.nanoTime();
        long currentTime = System.nanoTime();
        while(true){
            currentTime = System.nanoTime();
            if (currentTime - lastFrame >= timePerFrame) {
                
                lastFrame = System.nanoTime();
            }
        }
    }
    */
 
}
