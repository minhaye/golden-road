package goldenroad.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JPanel;

import goldenroad.entity.Item;
import goldenroad.entity.Monster;
import goldenroad.input.KeyHandler;
import goldenroad.scene.SceneManager;

public class GamePanel extends JPanel implements Runnable {
    public static final int SCREEN_WIDTH = 960;
    public static final int SCREEN_HEIGHT = 540;

    private static final int TARGET_FPS = 60;

    private static final int START_PLAYER_X = 120;
    private static final int START_PLAYER_Y = 380;
    private static final int PLAYER_WIDTH = 40;
    private static final int PLAYER_HEIGHT = 60;

    private static final double MOVE_SPEED = 4.0;
    private static final double GRAVITY = 1.1;
    private static final double JUMP_SPEED = -18.0;
    private static final double MAX_FALL_SPEED = 20.0;
    private static final int MAX_JUMPS = 2;
    private static final int TRANSITION_SPAWN_PADDING = 2;

    private final KeyHandler keyHandler = new KeyHandler();
    private final SceneManager sceneManager = new SceneManager();

    private double playerX = START_PLAYER_X;
    private double playerY = START_PLAYER_Y;
    private double velocityY = 0;
    private int jumpCount = 0;
    private boolean onGround = true;

    private Thread gameThread;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(new Color(20, 26, 38));
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(keyHandler);
    }

    public void startGameLoop() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
            requestFocusInWindow();
        }
    }

    @Override
    public void run() {
        double drawIntervalNs = 1_000_000_000.0 / TARGET_FPS;
        double delta = 0;
        long lastTime = System.nanoTime();

        while (gameThread != null) {
            long currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawIntervalNs;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void update() {
        double moveX = 0;
        if (keyHandler.leftPressed && !keyHandler.rightPressed) {
            moveX = -MOVE_SPEED;
        } else if (keyHandler.rightPressed && !keyHandler.leftPressed) {
            moveX = MOVE_SPEED;
        }

        applyHorizontalMovement(moveX);

        if (keyHandler.consumeJumpJustPressed() && jumpCount < MAX_JUMPS) {
            velocityY = JUMP_SPEED;
            jumpCount++;
            onGround = false;
        }

        velocityY += GRAVITY;
        if (velocityY > MAX_FALL_SPEED) {
            velocityY = MAX_FALL_SPEED;
        }

        applyVerticalMovement(velocityY);
    }

    private void applyHorizontalMovement(double deltaX) {
        if (deltaX == 0) {
            return;
        }

        double nextX = playerX + deltaX;
        Rectangle nextBounds = getPlayerBounds(nextX, playerY);

        for (Rectangle block : getCurrentSolidBlocks()) {
            if (nextBounds.intersects(block)) {
                if (deltaX > 0) {
                    nextX = block.x - PLAYER_WIDTH;
                } else {
                    nextX = block.x + block.width;
                }
                break;
            }
        }

        playerX = nextX;
        handleScreenTransition();
    }

    private void applyVerticalMovement(double deltaY) {
        double nextY = playerY + deltaY;
        Rectangle nextBounds = getPlayerBounds(playerX, nextY);

        onGround = false;

        for (Rectangle block : getCurrentSolidBlocks()) {
            if (nextBounds.intersects(block)) {
                if (deltaY > 0) {
                    nextY = block.y - PLAYER_HEIGHT;
                    onGround = true;
                } else if (deltaY < 0) {
                    nextY = block.y + block.height;
                }
                velocityY = 0;
                break;
            }
        }

        playerY = nextY;
        if (onGround) {
            jumpCount = 0;
        }
    }

    private Rectangle getPlayerBounds(double x, double y) {
        return new Rectangle((int) x, (int) y, PLAYER_WIDTH, PLAYER_HEIGHT);
    }

    private void handleScreenTransition() {
        if (playerX + PLAYER_WIDTH > SCREEN_WIDTH) {
            if (sceneManager.moveToRightScreen()) {
                playerX = TRANSITION_SPAWN_PADDING;
            } else {
                playerX = SCREEN_WIDTH - PLAYER_WIDTH;
            }
        }

        if (playerX < 0) {
            if (sceneManager.moveToLeftScreen()) {
                playerX = SCREEN_WIDTH - PLAYER_WIDTH - TRANSITION_SPAWN_PADDING;
            } else {
                playerX = 0;
            }
        }
    }

    private List<Rectangle> getCurrentSolidBlocks() {
        return sceneManager.getCurrentScreen().getSolidBlocks();
    }

    private List<Monster> getCurrentMonsters() {
        return sceneManager.getCurrentScreen().getMonsters();
    }

    private List<Item> getCurrentItems() {
        return sceneManager.getCurrentScreen().getItems();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(new Color(70, 110, 160));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(new Color(55, 45, 35));
        for (Rectangle block : getCurrentSolidBlocks()) {
            g.fillRect(block.x, block.y, block.width, block.height);
        }

        for (Item item : getCurrentItems()) {
            Rectangle itemBounds = item.getBounds();
            g.setColor(item.getColor());
            if (item.getShape() == Item.Shape.OVAL) {
                g.fillOval(itemBounds.x, itemBounds.y, itemBounds.width, itemBounds.height);
            } else {
                g.fillRect(itemBounds.x, itemBounds.y, itemBounds.width, itemBounds.height);
            }
        }

        for (Monster monster : getCurrentMonsters()) {
            Rectangle monsterBounds = monster.getBounds();
            g.setColor(monster.getColor());
            g.fillRect(monsterBounds.x, monsterBounds.y, monsterBounds.width, monsterBounds.height);
        }

        g.setColor(new Color(230, 190, 70));
        g.fillRect((int) playerX, (int) playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
    }
}
