package goldenroad.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;

import goldenroad.entity.Bullet;
import goldenroad.entity.Item;
import goldenroad.entity.Monster;
import goldenroad.input.KeyHandler;
import goldenroad.input.MouseHandler;
import goldenroad.scene.Screen;
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
    private static final double BULLET_SPEED = 10.0;
    private static final int NORMAL_BULLET_DIAMETER = 10;
    private static final int LARGE_BULLET_DIAMETER = 22;

    private final KeyHandler keyHandler = new KeyHandler();
    private final MouseHandler mouseHandler = new MouseHandler();
    private final SceneManager sceneManager = new SceneManager();
    private final List<Bullet> bullets = new ArrayList<>();

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
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
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
        handleShootingInput();
        updateBullets();
    }

    private void handleShootingInput() {
        if (mouseHandler.consumeLeftClick()) {
            spawnBullet(NORMAL_BULLET_DIAMETER, new Color(255, 245, 210));
        }

        if (mouseHandler.consumeRightClick()) {
            spawnBullet(LARGE_BULLET_DIAMETER, new Color(255, 150, 90));
        }
    }

    private void spawnBullet(int diameter, Color color) {
        double originX = playerX + (PLAYER_WIDTH / 2.0);
        double originY = playerY + (PLAYER_HEIGHT / 2.0);

        double directionX = mouseHandler.getMouseX() - originX;
        double directionY = mouseHandler.getMouseY() - originY;

        bullets.add(new Bullet(
            originX - (diameter / 2.0),
            originY - (diameter / 2.0),
            directionX,
            directionY,
            BULLET_SPEED,
            diameter,
            color
        ));
    }

    private void updateBullets() {
        Screen currentScreen = sceneManager.getCurrentScreen();
        Iterator<Bullet> bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update();

            Rectangle bulletBounds = bullet.getBounds();
            if (isOutOfScreen(bulletBounds) || collidesWithSolidBlock(bulletBounds)) {
                bulletIterator.remove();
                continue;
            }

            boolean hitMonster = false;
            for (Monster monster : currentScreen.getMonsters()) {
                if (bulletBounds.intersects(monster.getBounds())) {
                    currentScreen.removeMonster(monster);
                    bulletIterator.remove();
                    hitMonster = true;
                    break;
                }
            }

            if (hitMonster) {
                continue;
            }
        }
    }

    private boolean collidesWithSolidBlock(Rectangle bounds) {
        for (Rectangle block : getCurrentSolidBlocks()) {
            if (bounds.intersects(block)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOutOfScreen(Rectangle bounds) {
        return bounds.x + bounds.width < 0
            || bounds.x > SCREEN_WIDTH
            || bounds.y + bounds.height < 0
            || bounds.y > SCREEN_HEIGHT;
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
        boolean transitioned = false;

        if (playerX + PLAYER_WIDTH > SCREEN_WIDTH) {
            if (sceneManager.moveToRightScreen()) {
                playerX = TRANSITION_SPAWN_PADDING;
                transitioned = true;
            } else {
                playerX = SCREEN_WIDTH - PLAYER_WIDTH;
            }
        }

        if (playerX < 0) {
            if (sceneManager.moveToLeftScreen()) {
                playerX = SCREEN_WIDTH - PLAYER_WIDTH - TRANSITION_SPAWN_PADDING;
                transitioned = true;
            } else {
                playerX = 0;
            }
        }

        if (transitioned) {
            bullets.clear();
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

        for (Bullet bullet : bullets) {
            g.setColor(bullet.getColor());
            g.fillOval(bullet.getRenderX(), bullet.getRenderY(), bullet.getDiameter(), bullet.getDiameter());
        }

        g.setColor(new Color(230, 190, 70));
        g.fillRect((int) playerX, (int) playerY, PLAYER_WIDTH, PLAYER_HEIGHT);
    }
}
