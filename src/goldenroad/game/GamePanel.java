package goldenroad.game;

import goldenroad.entity.Bullet;
import goldenroad.entity.Item;
import goldenroad.entity.Monster;
import goldenroad.entity.Player;
import goldenroad.input.KeyHandler;
import goldenroad.input.MouseHandler;
import goldenroad.scene.SceneManager;
import goldenroad.scene.Screen;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


import javax.swing.JPanel;


public class GamePanel extends JPanel implements Runnable {
    //public static final int SCREEN_WIDTH = 960;
    //public static final int SCREEN_HEIGHT = 540;
    public static final int SCREEN_WIDTH = 640;
    public static final int SCREEN_HEIGHT = 360;
    public static final double SCREEN_SCALE = 1.25; // Adjust this value to scale the game (example: 1.25 for 1080p, 1.25/1.5 for 1440p)
    public double SCALE = 3 / SCREEN_SCALE; 
    // Default to 720p, can be changed to 1080p or 1440p by adjusting the denominator 
    // (example: for 1080p, use 1.25, for 1440p, use 1.25/1.5)
    // x2 = 1280x720 = 720p
    // x3 = 1920x1080 = 1080p
    // x4 = 2560x1440 = 1440p
    // x6 = 3840x2160 = 4K
    private static final int TARGET_FPS = 60;

    // CAMERA
    private double cameraX = 0;
    private double cameraY = 0;
    private final double DEADZONE_WIDTH = 150;
    private final double DEADZONE_HEIGHT = 120;
    private double lookAhead = 0;
    private double LOOK_AHEAD_DISTANCE = 50;

    private static final int START_PLAYER_X = 120; 
    private static final int START_PLAYER_Y = 380;
    private double PLAYER_WIDTH = 25 * SCALE;
    private double PLAYER_HEIGHT = 40 * SCALE;

    private static final int TRANSITION_SPAWN_PADDING = 2;      
    private double LASER_SPEED = 10.0 * SCALE;
    private static final int LASER_DIAMETER = 14;
    private static final int LASER_DAMAGE = 4;
    private static final Color LASER_COLOR = new Color(255, 90, 80);

    private static final double CLUSTER_BULLET_SPEED = 20.5;
    private static final int CLUSTER_BULLET_DIAMETER = 7;
    private static final int CLUSTER_BULLET_DAMAGE = 1;
    private static final int CLUSTER_BULLET_COUNT = 6;
    private static final double CLUSTER_SPREAD_DEGREES = 22.0;
    private static final Color CLUSTER_COLOR = new Color(255, 235, 160);
    // END OF PLAYER VARIABLES

    // INPUT HANDLERS, SCENE MANAGER, AND BULLET LIST
    private final KeyHandler keyHandler = new KeyHandler();
    private final MouseHandler mouseHandler = new MouseHandler();
    private final SceneManager sceneManager = new SceneManager();
    private final List<Bullet> bullets = new ArrayList<>();


    private Thread gameThread;

    private Player player;

    public void initPlayer() {
        player = new Player(120, 380); 
        player.update(keyHandler, getCurrentSolidBlocks());
    }

    public GamePanel() {
        setPanelSize();
        setBackground(new Color(20, 26, 38));
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(keyHandler);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
        initPlayer();
    }



    private void setPanelSize() {
        int scaledWidth = (int) (SCREEN_WIDTH * SCALE);
        int scaledHeight = (int) (SCREEN_HEIGHT * SCALE);
        Dimension size = new Dimension(scaledWidth, scaledHeight);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        revalidate();
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
            //updateFPS();
        }
    }

    private void update() {
        player.update(keyHandler, getCurrentSolidBlocks());

        updateCamera();

        handleShootingInput();
        updateBullets();

    }
    
    int worldWidth = 3000;
    int worldHeight = 1000;

 

private void updateCamera() {

    double halfW = (SCREEN_WIDTH * SCALE) / 2;
    double halfH = (SCREEN_HEIGHT * SCALE) / 2;

    // ===== 1. target mặc định (center player) =====
    double targetX = player.getX() - halfW;
    double targetY = player.getY() - halfH;

    // ===== 2. DEADZONE =====
    double screenCenterX = cameraX + halfW;
    double screenCenterY = cameraY + halfH;

    double dx = player.getX() - screenCenterX;
    double dy = player.getY() - screenCenterY;

    // chỉ điều chỉnh target khi vượt deadzone
    if (Math.abs(dx) > DEADZONE_WIDTH / 2) {
        targetX = cameraX + (dx > 0
                ? dx - DEADZONE_WIDTH / 2
                : dx + DEADZONE_WIDTH / 2);
    } else {
        targetX = cameraX; // giữ nguyên
    }

    if (Math.abs(dy) > DEADZONE_HEIGHT / 2) {
        targetY = cameraY + (dy > 0
                ? dy - DEADZONE_HEIGHT / 2
                : dy + DEADZONE_HEIGHT / 2);
    } else {
        targetY = cameraY;
    }

    // ===== 3. IDLE → kéo về center =====
    if (player.isIdle()) {
        double centerX = player.getX() - halfW;
        targetX += (centerX - targetX) * 0.5; // nhẹ hơn để mượt
    }
    
    // ===== 4. LERP CAMERA =====
    cameraX += (targetX - cameraX) * 0.1;
    cameraY += (targetY - cameraY) * 0.1;

    // ===== 5. CLAMP (optional) =====
    cameraX = Math.max(0, cameraX);
    cameraY = Math.max(0, cameraY);

    cameraX = Math.min(cameraX, worldWidth - SCREEN_WIDTH * SCALE);
    cameraY = Math.min(cameraY, worldHeight - SCREEN_HEIGHT * SCALE);
}

// Handle shooting input and bullet spawning
    private void handleShootingInput() {
        if (mouseHandler.consumeLeftClick()) {
            spawnLaserShot();
        }

        if (mouseHandler.consumeRightClick()) {
            spawnClusterShot();
        }
    }

 private void spawnLaserShot() {
    double originX = player.getX() + 25;
    double originY = player.getY() + 40;

    // 👉 convert mouse sang world space
    double worldMouseX = mouseHandler.getMouseX() + cameraX;
    double worldMouseY = mouseHandler.getMouseY() + cameraY;

    // 👉 dùng world space để tính direction
    double directionX = worldMouseX - originX;
    double directionY = worldMouseY - originY;

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
        double originX = player.getX() + 25;
        double originY = player.getY() + 40;

        double worldMouseX = mouseHandler.getMouseX() + cameraX;
        double worldMouseY = mouseHandler.getMouseY() + cameraY;

        double baseDirectionX = worldMouseX - originX;
        double baseDirectionY = worldMouseY - originY;

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

        double length = Math.sqrt(directionX * directionX + directionY * directionY);

        if (length != 0) {
            directionX /= length;
            directionY /= length;
        }

        bullets.add(new Bullet(
            originX - (diameter / 2.0),
            originY - (diameter / 2.0),
            directionX,
            directionY,
            speed,
            diameter,
            color,
            damage
        )
        );
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
            if (isOutOfScreen(bulletBounds) || collidesWithSolidBlock(bulletBounds)) {
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
            || bounds.x > SCREEN_WIDTH * SCALE
            || bounds.y + bounds.height < 0
            || bounds.y > SCREEN_HEIGHT * SCALE;
    }


    
    private void handleScreenTransition(float x, float y) {
        boolean transitioned = false;
        // Check if player has moved beyond the right edge of the screen
        if (x + PLAYER_WIDTH > SCREEN_WIDTH * SCALE ) { // If player's right edge goes beyond the screen width
            if (sceneManager.moveToRightScreen()) {
                x = TRANSITION_SPAWN_PADDING; 
                transitioned = true;
            } else {
                x = (float) (SCREEN_WIDTH * SCALE - PLAYER_WIDTH); 
            }
        }
        // Check if player has moved beyond the left edge of the screen
        if (x < 0) {
            if (sceneManager.moveToLeftScreen()) {
                x = (float)(SCREEN_WIDTH * SCALE - PLAYER_WIDTH - TRANSITION_SPAWN_PADDING);
                transitioned = true;
            } else {
                x = 0;
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
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(-cameraX, -cameraY);

        g.setColor(new Color(70, 110, 160));
        g.fillRect(0, 0, (int)(SCREEN_WIDTH * SCALE), (int)(SCREEN_HEIGHT * SCALE));

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
       
        
        player.draw((Graphics2D) g);
            
        
    }
}
