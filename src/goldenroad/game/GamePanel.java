package goldenroad.game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Polygon;
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
    private static final double SPRINT_MULTIPLIER = 1.9;
    private static final double GRAVITY = 1.1;
    private static final double JUMP_SPEED = -18.0;
    private static final double MAX_FALL_SPEED = 20.0;
    private static final int MAX_JUMPS = 2;
    private static final int TRANSITION_SPAWN_PADDING = 2;
    private static final int EXIT_DOOR_WIDTH = 44;
    private static final int EXIT_DOOR_HEIGHT = 72;
    private static final int EXIT_DOOR_MARGIN_RIGHT = 24;
    private static final double LASER_SPEED = 13.0;
    private static final int LASER_DIAMETER = 14;
    private static final int LASER_DAMAGE = 4;
    private static final Color LASER_COLOR = new Color(255, 90, 80);

    private static final double CLUSTER_BULLET_SPEED = 10.5;
    private static final int CLUSTER_BULLET_DIAMETER = 7;
    private static final int CLUSTER_BULLET_DAMAGE = 1;
    private static final int CLUSTER_BULLET_COUNT = 6;
    private static final double CLUSTER_SPREAD_DEGREES = 22.0;
    private static final Color CLUSTER_COLOR = new Color(255, 235, 160);

    private static final int CAMERA_DEAD_ZONE_WIDTH = 220;
    private static final int CAMERA_DEAD_ZONE_HEIGHT = 140;
    private static final double FIXED_DELTA_SECONDS = 1.0 / TARGET_FPS;

    private final KeyHandler keyHandler = new KeyHandler();
    private final MouseHandler mouseHandler = new MouseHandler();
    private final SceneManager sceneManager = new SceneManager();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();

    private double playerX = START_PLAYER_X;
    private double playerY = START_PLAYER_Y;
    private double velocityY = 0;
    private int jumpCount = 0;
    private boolean onGround = true;
    private double cameraX = 0;
    private double cameraY = 0;
    private boolean exitDoorVisible = false;

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
        double currentMoveSpeed = keyHandler.sprintPressed ? MOVE_SPEED * SPRINT_MULTIPLIER : MOVE_SPEED;
        double moveX = 0;
        if (keyHandler.leftPressed && !keyHandler.rightPressed) {
            moveX = -currentMoveSpeed;
        } else if (keyHandler.rightPressed && !keyHandler.leftPressed) {
            moveX = currentMoveSpeed;
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
        updateExitDoorVisibility();
        handleExitDoorTransition();
        updateCamera();
        handleShootingInput();
        updateBullets();
        updateMonsters();
        updateEnemyBullets();
    }

    private void handleShootingInput() {
        if (mouseHandler.consumeLeftClick()) {
            spawnLaserShot();
        }

        if (mouseHandler.consumeRightClick()) {
            spawnClusterShot();
        }
    }

    private void spawnLaserShot() {
        double originX = playerX + (PLAYER_WIDTH / 2.0);
        double originY = playerY + (PLAYER_HEIGHT / 2.0);

        double directionX = toWorldMouseX() - originX;
        double directionY = toWorldMouseY() - originY;

        spawnBullet(
            originX,
            originY,
            directionX,
            directionY,
            LASER_SPEED,
            LASER_DIAMETER,
            LASER_COLOR,
            LASER_DAMAGE
        );
    }

    private void spawnClusterShot() {
        double originX = playerX + (PLAYER_WIDTH / 2.0);
        double originY = playerY + (PLAYER_HEIGHT / 2.0);

        double baseDirectionX = toWorldMouseX() - originX;
        double baseDirectionY = toWorldMouseY() - originY;

        if (baseDirectionX == 0 && baseDirectionY == 0) {
            baseDirectionX = 1;
        }

        double spreadStep = CLUSTER_BULLET_COUNT == 1
            ? 0
            : CLUSTER_SPREAD_DEGREES / (CLUSTER_BULLET_COUNT - 1);
        double startAngle = -CLUSTER_SPREAD_DEGREES / 2.0;

        for (int i = 0; i < CLUSTER_BULLET_COUNT; i++) {
            double angleDegrees = startAngle + (spreadStep * i);
            double[] rotatedDirection = rotateVector(baseDirectionX, baseDirectionY, Math.toRadians(angleDegrees));

            spawnBullet(
                originX,
                originY,
                rotatedDirection[0],
                rotatedDirection[1],
                CLUSTER_BULLET_SPEED,
                CLUSTER_BULLET_DIAMETER,
                CLUSTER_COLOR,
                CLUSTER_BULLET_DAMAGE
            );
        }
    }

    private void spawnBullet(
        double originX,
        double originY,
        double directionX,
        double directionY,
        double speed,
        int diameter,
        Color color,
        int damage
    ) {

        bullets.add(new Bullet(
            originX - (diameter / 2.0),
            originY - (diameter / 2.0),
            directionX,
            directionY,
            speed,
            diameter,
            color,
            damage
        ));
    }

    private double[] rotateVector(double x, double y, double angleRadians) {
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);

        return new double[] {
            (x * cos) - (y * sin),
            (x * sin) + (y * cos)
        };
    }

    private void updateBullets() {
        Screen currentScreen = sceneManager.getCurrentScreen();
        Iterator<Bullet> bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update();

            Rectangle bulletBounds = bullet.getBounds();
            if (isOutOfWorld(bulletBounds) || collidesWithSolidBlock(bulletBounds)) {
                bulletIterator.remove();
                continue;
            }

            boolean hitMonster = false;
            for (Monster monster : currentScreen.getMonsters()) {
                if (bulletBounds.intersects(monster.getBounds())) {
                    if (monster.takeDamage(bullet.getDamage())) {
                        currentScreen.removeMonster(monster);
                    }
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

    private void updateMonsters() {
        Screen currentScreen = sceneManager.getCurrentScreen();
        double playerCenterX = playerX + (PLAYER_WIDTH / 2.0);
        double playerCenterY = playerY + (PLAYER_HEIGHT / 2.0);

        for (Monster monster : currentScreen.getMonsters()) {
            monster.update(playerCenterX, playerCenterY, FIXED_DELTA_SECONDS);
            Bullet shot = monster.createShotTowardPlayer(playerCenterX, playerCenterY);
            if (shot != null) {
                enemyBullets.add(shot);
            }
        }
    }

    private void updateEnemyBullets() {
        Iterator<Bullet> bulletIterator = enemyBullets.iterator();
        Rectangle playerBounds = getPlayerBounds(playerX, playerY);

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            bullet.update();

            Rectangle bulletBounds = bullet.getBounds();
            if (isOutOfWorld(bulletBounds) || collidesWithSolidBlock(bulletBounds)) {
                bulletIterator.remove();
                continue;
            }

            if (bulletBounds.intersects(playerBounds)) {
                bulletIterator.remove();
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

    private boolean isOutOfWorld(Rectangle bounds) {
        int worldWidth = getCurrentWorldWidth();
        int worldHeight = getCurrentWorldHeight();

        return bounds.x + bounds.width < 0
            || bounds.x > worldWidth
            || bounds.y + bounds.height < 0
            || bounds.y > worldHeight;
    }

    private void updateCamera() {
        double playerCenterX = playerX + (PLAYER_WIDTH / 2.0);
        double playerCenterY = playerY + (PLAYER_HEIGHT / 2.0);

        double deadZoneLeft = cameraX + ((SCREEN_WIDTH - CAMERA_DEAD_ZONE_WIDTH) / 2.0);
        double deadZoneRight = deadZoneLeft + CAMERA_DEAD_ZONE_WIDTH;
        if (playerCenterX < deadZoneLeft) {
            cameraX = playerCenterX - ((SCREEN_WIDTH - CAMERA_DEAD_ZONE_WIDTH) / 2.0);
        } else if (playerCenterX > deadZoneRight) {
            cameraX = playerCenterX - ((SCREEN_WIDTH + CAMERA_DEAD_ZONE_WIDTH) / 2.0);
        }

        double deadZoneTop = cameraY + ((SCREEN_HEIGHT - CAMERA_DEAD_ZONE_HEIGHT) / 2.0);
        double deadZoneBottom = deadZoneTop + CAMERA_DEAD_ZONE_HEIGHT;
        if (playerCenterY < deadZoneTop) {
            cameraY = playerCenterY - ((SCREEN_HEIGHT - CAMERA_DEAD_ZONE_HEIGHT) / 2.0);
        } else if (playerCenterY > deadZoneBottom) {
            cameraY = playerCenterY - ((SCREEN_HEIGHT + CAMERA_DEAD_ZONE_HEIGHT) / 2.0);
        }

        cameraX = clamp(cameraX, 0, Math.max(0, getCurrentWorldWidth() - SCREEN_WIDTH));
        cameraY = clamp(cameraY, 0, Math.max(0, getCurrentWorldHeight() - SCREEN_HEIGHT));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
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
        playerX = clamp(playerX, 0, getCurrentWorldWidth() - PLAYER_WIDTH);
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

    private void updateExitDoorVisibility() {
        exitDoorVisible = sceneManager.canMoveToRightScreen();
    }

    private Rectangle getExitDoorBounds() {
        int worldWidth = getCurrentWorldWidth();

        int doorX = worldWidth - EXIT_DOOR_WIDTH - EXIT_DOOR_MARGIN_RIGHT;
        int floorTopY = getDoorFloorTopY(doorX, EXIT_DOOR_WIDTH);
        int doorY = floorTopY - EXIT_DOOR_HEIGHT;

        return new Rectangle(doorX, doorY, EXIT_DOOR_WIDTH, EXIT_DOOR_HEIGHT);
    }

    private int getDoorFloorTopY(int doorX, int doorWidth) {
        int floorTopY = Integer.MIN_VALUE;

        for (Rectangle block : getCurrentSolidBlocks()) {
            boolean overlapsDoor = block.x < doorX + doorWidth && block.x + block.width > doorX;
            if (overlapsDoor && block.y > floorTopY) {
                floorTopY = block.y;
            }
        }

        if (floorTopY == Integer.MIN_VALUE) {
            floorTopY = getCurrentWorldHeight() - EXIT_DOOR_HEIGHT - 12;
        }

        return floorTopY;
    }

    private void handleExitDoorTransition() {
        if (!exitDoorVisible) {
            return;
        }

        Rectangle playerBounds = getPlayerBounds(playerX, playerY);
        if (!playerBounds.intersects(getExitDoorBounds())) {
            return;
        }

        if (!keyHandler.consumeInteractJustPressed()) {
            return;
        }

        if (!sceneManager.moveToRightScreen()) {
            return;
        }

        playerX = TRANSITION_SPAWN_PADDING;
        velocityY = 0;
        jumpCount = 0;
        onGround = false;
        bullets.clear();
        enemyBullets.clear();
        cameraX = 0;
        cameraY = 0;
        exitDoorVisible = false;
    }

    private int getCurrentWorldWidth() {
        return sceneManager.getCurrentScreen().getWorldWidth();
    }

    private int getCurrentWorldHeight() {
        return sceneManager.getCurrentScreen().getWorldHeight();
    }

    private double toWorldMouseX() {
        return mouseHandler.getMouseX() + cameraX;
    }

    private double toWorldMouseY() {
        return mouseHandler.getMouseY() + cameraY;
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

        int renderOffsetX = (int) Math.round(cameraX);
        int renderOffsetY = (int) Math.round(cameraY);

        g.setColor(new Color(70, 110, 160));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(new Color(55, 45, 35));
        for (Rectangle block : getCurrentSolidBlocks()) {
            g.fillRect(block.x - renderOffsetX, block.y - renderOffsetY, block.width, block.height);
        }

        for (Item item : getCurrentItems()) {
            Rectangle itemBounds = item.getBounds();
            g.setColor(item.getColor());
            if (item.getShape() == Item.Shape.OVAL) {
                g.fillOval(itemBounds.x - renderOffsetX, itemBounds.y - renderOffsetY, itemBounds.width, itemBounds.height);
            } else {
                g.fillRect(itemBounds.x - renderOffsetX, itemBounds.y - renderOffsetY, itemBounds.width, itemBounds.height);
            }
        }

        for (Monster monster : getCurrentMonsters()) {
            Rectangle monsterBounds = monster.getBounds();
            g.setColor(monster.getColor());
            drawMonster(g, monster, monsterBounds, renderOffsetX, renderOffsetY);
        }

        for (Bullet bullet : bullets) {
            g.setColor(bullet.getColor());
            g.fillOval(bullet.getRenderX() - renderOffsetX, bullet.getRenderY() - renderOffsetY, bullet.getDiameter(), bullet.getDiameter());
        }

        for (Bullet bullet : enemyBullets) {
            g.setColor(bullet.getColor());
            g.fillOval(bullet.getRenderX() - renderOffsetX, bullet.getRenderY() - renderOffsetY, bullet.getDiameter(), bullet.getDiameter());
        }

        g.setColor(new Color(230, 190, 70));
        g.fillRect((int) playerX - renderOffsetX, (int) playerY - renderOffsetY, PLAYER_WIDTH, PLAYER_HEIGHT);

        if (exitDoorVisible) {
            Rectangle doorBounds = getExitDoorBounds();

            g.setColor(new Color(40, 60, 90));
            g.fillRect(doorBounds.x - renderOffsetX, doorBounds.y - renderOffsetY, doorBounds.width, doorBounds.height);

            g.setColor(new Color(230, 245, 255));
            g.drawRect(doorBounds.x - renderOffsetX, doorBounds.y - renderOffsetY, doorBounds.width, doorBounds.height);

            g.setColor(new Color(245, 215, 130));
            int knobSize = 7;
            int knobX = doorBounds.x + doorBounds.width - 12;
            int knobY = doorBounds.y + (doorBounds.height / 2);
            g.fillOval(knobX - renderOffsetX, knobY - renderOffsetY, knobSize, knobSize);

            Rectangle playerBounds = getPlayerBounds(playerX, playerY);
            if (playerBounds.intersects(doorBounds)) {
                g.setColor(new Color(255, 255, 255));
                g.drawString("Press E to enter", doorBounds.x - renderOffsetX - 22, doorBounds.y - renderOffsetY - 8);
            }
        }

        g.setColor(new Color(255, 255, 255, 55));
        int deadZoneRenderX = (SCREEN_WIDTH - CAMERA_DEAD_ZONE_WIDTH) / 2;
        int deadZoneRenderY = (SCREEN_HEIGHT - CAMERA_DEAD_ZONE_HEIGHT) / 2;
        g.drawRect(deadZoneRenderX, deadZoneRenderY, CAMERA_DEAD_ZONE_WIDTH, CAMERA_DEAD_ZONE_HEIGHT);
    }

    private void drawMonster(Graphics g, Monster monster, Rectangle bounds, int renderOffsetX, int renderOffsetY) {
        int x = bounds.x - renderOffsetX;
        int y = bounds.y - renderOffsetY;

        switch (monster.getRenderShape()) {
            case OVAL:
                g.fillOval(x, y, bounds.width, bounds.height);
                break;
            case TRIANGLE:
                Polygon triangle = new Polygon(
                    new int[] { x + (bounds.width / 2), x, x + bounds.width },
                    new int[] { y, y + bounds.height, y + bounds.height },
                    3
                );
                g.fillPolygon(triangle);
                break;
            default:
                g.fillRect(x, y, bounds.width, bounds.height);
                break;
        }
    }
}
