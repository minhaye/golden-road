package goldenroad.game;

import goldenroad.entity.Bullet;
import goldenroad.entity.Item;
import goldenroad.entity.Monster;
import goldenroad.entity.Player;
import goldenroad.input.KeyHandler;
import goldenroad.input.MouseHandler;
import goldenroad.map.CollisionHandler;
import goldenroad.map.CollisionMap;
import goldenroad.scene.SceneManager;
import goldenroad.scene.Screen;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JPanel;


public class GamePanel extends JPanel implements Runnable {
    //public static final int SCREEN_WIDTH = 960;
    //public static final int SCREEN_HEIGHT = 540;
    public static final int SCREEN_WIDTH = 640;
    public static final int SCREEN_HEIGHT = 360;
    private static final double SCREEN_SCALE = 3;
    public double SCALE = 2.4; 
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
    private final double DEADZONE_HEIGHT = 150;
    
    // MAP
    private CollisionMap collisionMap;
    private CollisionHandler collisionHandler;
    private BufferedImage mapImage;
    private final int WORLD_WIDTH = 4480;
    private final int WORLD_HEIGHT = 2560;
        
    int worldWidth =    (int) (WORLD_WIDTH  * SCALE);
    int worldHeight =   (int) (WORLD_HEIGHT * SCALE);
    
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
        player = new Player(2220, 4680); 
        player.update(keyHandler);
    }

    public void loadMap() {
    try {
        var stream = getClass().getResourceAsStream("/assets/map/BIG_INTRO_ROOM.png");

        if (stream == null) {
            System.out.println("Không tìm thấy map!");
            return;
        }

        mapImage = ImageIO.read(stream);
       // 👉 load collision
        collisionMap = new CollisionMap();
        collisionMap.load("/assets/map/BIG_INTRO_ROOM_COLLISION.png");

        collisionHandler = new CollisionHandler(collisionMap, SCALE);

        System.out.println("Load map + collision OK");



    } catch (Exception e) {
        e.printStackTrace();
    }
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
        int scaledWidth = (int) (SCREEN_WIDTH * SCREEN_SCALE);
        int scaledHeight = (int) (SCREEN_HEIGHT * SCREEN_SCALE);
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
        player.update(keyHandler);

        collisionHandler.move(
        player,
        player.getVelocityX(),
        player.getVelocityY()
    );

        updateCamera();

        handleShootingInput();
        updateBullets();

    }

 

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
    cameraY += (targetY - cameraY) * 0.5;

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
            || bounds.x > SCREEN_WIDTH * SCREEN_SCALE
            || bounds.y + bounds.height < 0
            || bounds.y > SCREEN_HEIGHT * SCREEN_SCALE;
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

        /* 
        g.setColor(new Color(70, 110, 160));
        g.fillRect(0, 0, (int)(SCREEN_WIDTH * SCREEN_SCALE), (int)(SCREEN_HEIGHT * SCREEN_SCALE));

        g.setColor(new Color(55, 45, 35));
        for (Rectangle block : getCurrentSolidBlocks()) {
            g.fillRect(block.x, block.y, block.width, block.height);
        }
            */

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
       
        // DRAW MAP
        
        if (mapImage != null) {
            g2.drawImage(mapImage, -0, -0, worldWidth, worldHeight, null);
        }


        // DRAW MIRAI
        player.draw((Graphics2D) g);
            
        g.setColor(new Color(150, 110, 160));
        g.fillRect(0, 0, (int)(100), (int)(100));
    }
}
