package goldenroad.game;

import goldenroad.audio.AudioManager;
import goldenroad.entity.Bullet;
import goldenroad.entity.Item;
import goldenroad.entity.Monster;
import goldenroad.entity.Player;
import goldenroad.input.KeyHandler;
import goldenroad.input.MouseHandler;
import goldenroad.map.CollisionHandler;
import goldenroad.map.CollisionMap;
import goldenroad.map.MonsterCollisionHandler;
import goldenroad.scene.Menu;
import goldenroad.scene.SceneManager;
import goldenroad.scene.Screen;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
    // public static final int SCREEN_WIDTH = 960;
    // public static final int SCREEN_HEIGHT = 540;
    public static final int SCREEN_WIDTH = 640;
    public static final int SCREEN_HEIGHT = 360;
    public static final int TILE_SIZE = 16;
    private static final int WINDOW_SCALE = 3;
    // Default to 720p, can be changed to 1080p or 1440p by adjusting the
    // denominator
    // (example: for 1080p, use 1.25, for 1440p, use 1.25/1.5)
    // x2 = 1280x720 = 720p
    // x3 = 1920x1080 = 1080p
    // x4 = 2560x1440 = 1440p
    // x6 = 3840x2160 = 4K
    private static final int TARGET_FPS = 60;

    // CAMERA
    private double cameraX = 0;
    private double cameraY = 0;
    private double lookAheadX = 0;

    // MAP
    private CollisionMap collisionMap;
    private CollisionHandler collisionHandler;
    private MonsterCollisionHandler monsterCollisionHandler;
    private BufferedImage mapImage, hiddenImage, gameBuffer;
    private Graphics2D bufferG;
    private boolean mapLoaded = false;

    private int WORLD_WIDTH = 330 * TILE_SIZE;
    private int WORLD_HEIGHT = 140 * TILE_SIZE;

    int worldWidth = WORLD_WIDTH;
    int worldHeight = WORLD_HEIGHT;

    // GUN + AIM
    private double renderScale;
    private int renderOffsetX;
    private int renderOffsetY;

    private int leftShootCooldown = 0;
    private int rightShootCooldown = 0;

    private static final int LEFT_SHOOT_DELAY = 15;
    private static final int RIGHT_SHOOT_DELAY = 60;

    private double LASER_SPEED = 15;
    private static final int LASER_DIAMETER = 10;
    private static final int LASER_DAMAGE = 4;
    private static final Color LASER_COLOR = new Color(255, 90, 80);

    private static final double CLUSTER_BULLET_SPEED = 15;
    private static final int CLUSTER_BULLET_DIAMETER = 7;
    private static final int CLUSTER_BULLET_DAMAGE = 1;
    private static final int CLUSTER_BULLET_COUNT = 6;
    private static final double CLUSTER_SPREAD_DEGREES = 30.0;
    private static final Color CLUSTER_COLOR = new Color(255, 235, 160);
    // END OF PLAYER VARIABLES

    // INPUT HANDLERS, SCENE MANAGER, AND BULLET LIST
    private final KeyHandler keyHandler = new KeyHandler();
    private final MouseHandler mouseHandler = new MouseHandler();
    private final SceneManager sceneManager = new SceneManager();
    private final Menu menu = new Menu(this);
    private final List<Bullet> bullets = new ArrayList<>();

    // Audio
    private final AudioManager audio = new AudioManager();
    private boolean musicStarted = false;

    private Thread gameThread;

    private Player player;

    public void initPlayer() {
        player = new Player(400, 1995);
        player.update(keyHandler);
    }

    public void loadMap() {
        try {
            mapLoaded = false;
            var stream = getClass().getResourceAsStream("/assets/map/ROOM_1.png");
            var stream1 = getClass().getResourceAsStream("/assets/map/ROOM_1_HIDDEN.png");

            if (stream == null) {
                System.out.println("Không tìm thấy map!");
                return;
            }
            if (stream1 == null) {
                System.out.println("Không tìm thấy hidden map!");
                return;
            }

            mapImage = ImageIO.read(stream);
            hiddenImage = ImageIO.read(stream1);

            // load collision
            collisionMap = new CollisionMap();
            collisionMap.load("/assets/map/ROOM_1_COLLISION.png");

            collisionHandler = new CollisionHandler(collisionMap);
            monsterCollisionHandler = new MonsterCollisionHandler(collisionMap);
            mapLoaded = true;

            System.out.println("Load map + collision OK");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean ensureMapLoaded() {
        if (!mapLoaded || collisionHandler == null || monsterCollisionHandler == null || mapImage == null) {
            loadMap();
        }

        return mapLoaded && collisionHandler != null && monsterCollisionHandler != null && mapImage != null;
    }

    public GamePanel() {
        gameBuffer = new BufferedImage(
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                BufferedImage.TYPE_INT_ARGB);

        bufferG = gameBuffer.createGraphics();
        bufferG.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        setPanelSize();
        setBackground(new Color(20, 26, 38));
        setDoubleBuffered(true);
        setFocusable(true);

        addKeyListener(keyHandler);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        initPlayer();

        audio.loadMusic("main", "/assets/audio/Pixel 1.wav");

        audio.loadSfx("shoot", "/assets/audio/Pixel 2.wav");
        audio.loadSfx("shotgun", "/assets/audio/Pixel 3.wav");
        audio.loadSfx("hit", "/assets/audio/Pixel 4.wav");
        audio.loadSfx("player_hurt", "/assets/audio/Pixel 5.wav");
        audio.loadSfx("menu_click", "/assets/audio/Pixel 6.wav");
        menu.update(mouseHandler);
    }

    private void setPanelSize() {
        Dimension size = new Dimension(
                SCREEN_WIDTH * WINDOW_SCALE,
                SCREEN_HEIGHT * WINDOW_SCALE);

        setPreferredSize(size);
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
        // If menu active, let it handle input and block gameplay updates
        if (menu.isActive()) {
            menu.update(mouseHandler);
            return;
        }

        if (!ensureMapLoaded()) {
            return;
        }

        if (!musicStarted) {
            audio.playMusic("main");
            musicStarted = true;
        }
        player.update(keyHandler);

        collisionHandler.move(
                player,
                player.getVelocityX(),
                player.getVelocityY());

        updateCamera();
        handleShootingInput();
        updateBullets();
        updateMonsters();

        if (leftShootCooldown > 0)
            leftShootCooldown--;
        if (rightShootCooldown > 0)
            rightShootCooldown--;

    }

    private static final double LOOK_AHEAD_DISTANCE = 120;

    private void updateCamera() {

        double halfW = SCREEN_WIDTH / 2;
        double halfH = SCREEN_HEIGHT / 2;

        // ===== LOOK AHEAD =====

        double targetLookAhead = 0;

        if (player.getVelocityX() > 0) {
            targetLookAhead = LOOK_AHEAD_DISTANCE;
        } else if (player.getVelocityX() < 0) {
            targetLookAhead = -LOOK_AHEAD_DISTANCE;
        }

        lookAheadX += (targetLookAhead - lookAheadX) * 0.04;

        // ===== CAMERA TARGET =====

        double targetX = player.getX() - halfW + lookAheadX;
        double targetY = player.getY() - halfH;

        // ===== SOFT DEADZONE =====

        double dx = targetX - cameraX;

        if (Math.abs(dx) > 1) {
            cameraX += dx * 0.08;
        }

        double dy = targetY - cameraY;

        if (Math.abs(dy) > 1) {
            cameraY += dy * 0.08;
        }

        // ===== IDLE → kéo về center =====

        double centerX = player.getX() - halfW;
        targetX += (centerX - targetX) * 0.5; // Thấp hơn = mượt và chậm hơn

        // ===== LERP CAMERA =====
        cameraX += (targetX - cameraX) * 0.1;
        cameraY += (targetY - cameraY) * 0.5;

        // ===== CLAMP =====
        cameraX = Math.max(0, cameraX);
        cameraY = Math.max(0, cameraY);

        cameraX = Math.min(cameraX, worldWidth - SCREEN_WIDTH);
        cameraY = Math.min(cameraY, worldHeight - SCREEN_HEIGHT);
    }

    // Handle shooting input and bullet spawning
    private void handleShootingInput() {

        // ===== LEFT CLICK =====
        if (mouseHandler.isLeftPressed() && leftShootCooldown <= 0) {
            spawnLaserShot();
            leftShootCooldown = LEFT_SHOOT_DELAY;
        }

        // ===== RIGHT CLICK =====
        if (mouseHandler.isRightPressed() && rightShootCooldown <= 0) {
            spawnClusterShot();
            rightShootCooldown = RIGHT_SHOOT_DELAY;
        }
    }

    private void spawnLaserShot() {
        double originX = player.getX() + 25;
        double originY = player.getY() + 15;

        // convert mouse sang world space
        double worldMouseX = getMouseWorldX();
        double worldMouseY = getMouseWorldY();

        // dùng world space để tính direction
        double directionX = worldMouseX - originX;
        double directionY = worldMouseY - originY;

        audio.playSfx("shoot");
        spawnBullet(
                originX,
                originY,
                directionX,
                directionY,
                LASER_SPEED,
                LASER_DIAMETER,
                LASER_COLOR,
                LASER_DAMAGE);
    }

    private void spawnClusterShot() {

        double originX = player.getX() + 10;
        double originY = player.getY() + 15;

        double worldMouseX = getMouseWorldX();
        double worldMouseY = getMouseWorldY();

        double baseDirectionX = worldMouseX - originX;
        double baseDirectionY = worldMouseY - originY;

        if (baseDirectionX == 0 && baseDirectionY == 0) {
            baseDirectionX = 1;
        }

        audio.playSfx("shotgun");

        for (int i = 0; i < CLUSTER_BULLET_COUNT; i++) {

            // ===== RANDOM SPREAD =====
            double randomAngle = Math.toRadians((Math.random() - 0.5) * CLUSTER_SPREAD_DEGREES);

            double[] dir = rotateVector(
                    baseDirectionX,
                    baseDirectionY,
                    randomAngle);

            // ===== RANDOM SPEED =====
            double speed = CLUSTER_BULLET_SPEED + (Math.random() * 4 - 2);

            // ===== RANDOM SPAWN OFFSET =====
            double spawnOffsetX = (Math.random() - 0.5) * 8;
            double spawnOffsetY = (Math.random() - 0.5) * 8;

            spawnBullet(
                    originX + spawnOffsetX,
                    originY + spawnOffsetY,
                    dir[0],
                    dir[1],
                    speed,
                    CLUSTER_BULLET_DIAMETER,
                    CLUSTER_COLOR,
                    CLUSTER_BULLET_DAMAGE);
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
            int damage) {

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
                damage,
                collisionMap));
    }

    private double[] rotateVector(double x, double y, double angleRadians) {
        double cos = Math.cos(angleRadians);
        double sin = Math.sin(angleRadians);

        return new double[] {
                (x * cos) - (y * sin),
                (x * sin) + (y * cos)
        };
    }

    private double getMouseWorldX() {
        double mouseX = (mouseHandler.getMouseX() - renderOffsetX) / renderScale;
        return mouseX + cameraX;
    }

    private double getMouseWorldY() {

        double mouseY = (mouseHandler.getMouseY() - renderOffsetY) / renderScale;
        return mouseY + cameraY;
    }

    private void updateBullets() {
        Screen currentScreen = sceneManager.getCurrentScreen();
        Iterator<Bullet> bulletIterator = bullets.iterator();

        while (bulletIterator.hasNext()) {
            Bullet bullet = bulletIterator.next();
            if (bullet.isDestroyed()) {
                bulletIterator.remove();
                continue;
            }

            Rectangle bulletBounds = bullet.getBounds();
            if (isOutOfScreen(bulletBounds) || collidesWithSolidBlock(bulletBounds)) {
                bulletIterator.remove();
                continue;
            }

            boolean hitMonster = false;
            for (Monster monster : currentScreen.getMonsters()) {
                if (bulletBounds.intersects(monster.getBounds())) {
                    audio.playSfx("hit");
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
            bullet.update();
        }
    }

    private void updateMonsters() {
        for (Monster monster : getCurrentMonsters()) {
            // ===== INITIALIZE AI (lazy) =====
            if (monster.getMonsterAI() == null) {
                monster.initializeAI(collisionMap);

                // Đặt AttackListener để xử lý damage player
                if (monster.getMonsterAI() != null) {
                    monster.getMonsterAI().setAttackListener((m, damage) -> {
                        audio.playSfx("player_hurt");
                        player.takeDamage(damage);
                    });
                }
            }

            // ===== UPDATE AI =====
            if (monster.getMonsterAI() != null) {
                double playerCenterX = player.getX() + player.getWidth() / 2.0;
                double playerCenterY = player.getY() + player.getHeight() / 2.0;

                monster.getMonsterAI().update(
                        monster,
                        player.getX(),
                        player.getY(),
                        (int) player.getWidth(),
                        (int) player.getHeight());
            } else {
                // Fallback: simple patrol nếu không có AI
                monster.applyGravity();
                if (monster.getState() == Monster.State.IDLE) {
                    monster.incrementIdleTimer();
                    if (monster.getIdleTimer() >= monster.getIdleTimeoutMax()) {
                        monster.setState(Monster.State.PATROL);
                        monster.resetIdleTimer();
                    }
                } else if (monster.getState() == Monster.State.PATROL) {
                    double patrolVx = monster.computePatrolVelocity();
                    monster.setVelocityX(patrolVx);
                }
                monsterCollisionHandler.move(monster, monster.getVelocityX(), monster.getVelocityY());
            }
            // ===== UPDATE ANIMATION =====
            monster.updateAnimation();
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

        return bounds.x + bounds.width < cameraX
                || bounds.x > cameraX + SCREEN_WIDTH
                || bounds.y + bounds.height < cameraY
                || bounds.y > cameraY + SCREEN_HEIGHT;
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

        // ===== RESET =====
        bufferG.setTransform(new java.awt.geom.AffineTransform());

        // ===== CLEAR =====
        bufferG.setColor(getBackground());
        bufferG.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        if (menu.isActive()) {

            menu.render(bufferG);

        } else {

            // ===== CAMERA =====
            bufferG.translate(
                    -(int) cameraX,
                    -(int) cameraY);

            // ===== MAP =====
            if (mapImage != null) {
                bufferG.drawImage(mapImage, 0, 0, null);
            }

            // ===== ITEMS =====
            for (Item item : getCurrentItems()) {

                Rectangle r = item.getBounds();
                bufferG.setColor(item.getColor());

                if (item.getShape() == Item.Shape.OVAL) {
                    bufferG.fillOval(r.x, r.y, r.width, r.height);
                } else {
                    bufferG.fillRect(r.x, r.y, r.width, r.height);
                }
            }

            // ===== MONSTERS =====
            for (Monster monster : getCurrentMonsters()) {
                monster.draw(bufferG);
            }

            // ===== BULLETS =====
            for (Bullet bullet : bullets) {

                bufferG.setColor(bullet.getColor());

                bufferG.fillOval(
                        bullet.getRenderX(),
                        bullet.getRenderY(),
                        bullet.getDiameter(),
                        bullet.getDiameter());
            }

            // ===== PLAYER =====
            player.draw(bufferG);

            // ===== HIDDEN LAYER =====
            if (hiddenImage != null) {
                bufferG.drawImage(hiddenImage, 0, 0, null);
            }

            // reset transform
            bufferG.setTransform(new java.awt.geom.AffineTransform());

            menu.render(bufferG);
        }

        // ===== DRAW BUFFER TO SCREEN =====

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // ===== SCALE GIỮ TỈ LỆ =====
        double scaleX = (double) panelWidth / SCREEN_WIDTH;
        double scaleY = (double) panelHeight / SCREEN_HEIGHT;

        double scale = Math.min(scaleX, scaleY);

        // ===== SIZE SAU SCALE =====
        int renderWidth = (int) (SCREEN_WIDTH * scale);
        int renderHeight = (int) (SCREEN_HEIGHT * scale);

        // ===== CENTER =====
        int x = (panelWidth - renderWidth) / 2;
        int y = (panelHeight - renderHeight) / 2;

        renderScale = scale;
        renderOffsetX = x;
        renderOffsetY = y;

        // ===== BLACK BAR =====
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, panelWidth, panelHeight);

        // ===== DRAW GAME =====
        g2.drawImage(
                gameBuffer,
                x,
                y,
                renderWidth,
                renderHeight,
                null);
    }

    // CAWL AND BAWLS
    // Click sound effect for menu interactions
    public void playMenuClickSound() {
        audio.playSfx("menu_click");
    }

    public void setMusicVolume(float volume) {
        audio.setMusicVolume(volume);
    }

    public float getMusicVolume() {
        return audio.getMusicVolume();
    }

    public void setSfxVolume(float volume) {
        audio.setSfxVolume(volume);
    }

    public float getSfxVolume() {
        return audio.getSfxVolume();
    }
}
