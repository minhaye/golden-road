package goldenroad.game;

import goldenroad.entity.Bullet;
import goldenroad.entity.Bullet.BulletType;
import goldenroad.entity.Inventory;
import goldenroad.entity.Item;
import goldenroad.entity.Monster;
import goldenroad.entity.Player;
import goldenroad.input.KeyHandler;
import goldenroad.input.MouseHandler;
import goldenroad.map.CollisionHandler;
import goldenroad.map.CollisionMap;
import goldenroad.scene.SceneManager;
import goldenroad.scene.Screen;
import goldenroad.scene.Menu;
import goldenroad.ui.Hud;
import goldenroad.ui.InventoryPanel;
import goldenroad.ui.UiTheme;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.util.Collections;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JPanel;


public class GamePanel extends JPanel implements Runnable {
    //public static final int SCREEN_WIDTH = 960;
    //public static final int SCREEN_HEIGHT = 540;
    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 440;
    public static final int TILE_SIZE = 16;
    private static final int WINDOW_SCALE = 3;
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
    private double lookAheadX = 0;
    
    //PARALLAX
    private BufferedImage[] parallaxLayers;

    // MAP
    private CollisionMap collisionMap;
    private CollisionHandler collisionHandler;
    private BufferedImage mapImage,hiddenImage,gameBuffer;
    private Graphics2D bufferG;
    private BufferedImage hpItemSprite;
    private BufferedImage mpItemSprite;
    private BufferedImage keyItemSprite;

    private String toastMessage = null;
    private long toastExpireAtNanos = 0L;

    private  int WORLD_WIDTH = 330 * TILE_SIZE;
    private  int WORLD_HEIGHT = 180 * TILE_SIZE;
    // Size: 
    // Map 00: 280 x 160 
    // Map 01: 330 x 140 
    // Map 02: 180 x 300 
        
    int worldWidth  =   WORLD_WIDTH;
    int worldHeight =   WORLD_HEIGHT;

    // GUN + AIM
    private double renderScale;
    private int renderOffsetX;
    private int renderOffsetY;

    private int leftShootCooldown = 0;
    private int rightShootCooldown = 0;

    private static final int LEFT_SHOOT_DELAY = 18;     // 0.5s at 60fps = 30 frames
    private static final int RIGHT_SHOOT_DELAY = 120;
    private static final int LEFT_SHOOT_MP_COST = 5;
    private static final int RIGHT_SHOOT_MP_COST = 30;
    
    private double LASER_SPEED = 15;
    private static final int LASER_DIAMETER = 10;
    private static final int LASER_DAMAGE = 4;
    private static final Color LASER_COLOR = new Color(255, 90, 80);

    private static final double CLUSTER_BULLET_SPEED = 15;
    private static final int CLUSTER_BULLET_DIAMETER = 6;
    private static final int CLUSTER_BULLET_DAMAGE = 1;
    private static final int CLUSTER_BULLET_COUNT = 10;
    private static final double CLUSTER_SPREAD_DEGREES = 30.0;
    private static final Color CLUSTER_COLOR = new Color(255, 235, 160);
    // END OF PLAYER VARIABLES

    // INPUT HANDLERS, SCENE MANAGER, AND BULLET LIST
    private final KeyHandler keyHandler = new KeyHandler();
    private final MouseHandler mouseHandler = new MouseHandler();
    private final SceneManager sceneManager = new SceneManager();
    private final Menu menu = new Menu(this);
    private final List<Bullet> bullets = new ArrayList<>();
    private final Inventory inventory = new Inventory();
    private Hud hud;
    private InventoryPanel inventoryPanel;


    private Thread gameThread;

    private Player player;

    //Spawn: 
    // Map 00: 
    // Map 01:
    // Map 02: 400, 4000

    public void initPlayer() {
        player = new Player(400, 2000);
        player.update(keyHandler);
        hud = new Hud(this, player, inventory);
        inventoryPanel = new InventoryPanel(inventory, player);
    }

    private BufferedImage loadSprite(String resourcePath) {
        try {
            var stream = getClass().getResourceAsStream(resourcePath);
            if (stream == null) {
                System.out.println("Không tìm thấy resource: " + resourcePath);
                return null;
            }
            return ImageIO.read(stream);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadItemSprites() {
        hpItemSprite = loadSprite("/assets/item/hp.png");
        mpItemSprite = loadSprite("/assets/item/mp.png");
        keyItemSprite = loadSprite("/assets/item/key.png");
    }

    private BufferedImage getItemSprite(Item.ItemType type) {
        return switch (type) {
            case HP_POTION -> hpItemSprite;
            case MP_POTION -> mpItemSprite;
            case KEY -> keyItemSprite;
        };
    }


    public void showToast(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        toastMessage = message;
        toastExpireAtNanos = System.nanoTime() + 2_000_000_000L;
    }

    private void renderToast(Graphics2D g2) {
        if (toastMessage == null) {
            return;
        }

        if (System.nanoTime() >= toastExpireAtNanos) {
            toastMessage = null;
            return;
        }

        UiTheme.enableTextAntialiasing(g2);
        g2.setComposite(AlphaComposite.SrcOver);

        int panelWidth = UiTheme.BASE_W;
        int panelHeight = UiTheme.BASE_H;

        g2.setFont(UiTheme.FONT_BODY);
        int textWidth = g2.getFontMetrics().stringWidth(toastMessage);
        int boxWidth = Math.max(180, textWidth + 24);
        int boxHeight = 28;
        int x = (panelWidth - boxWidth) / 2;
        int y = panelHeight - 42;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(x, y, boxWidth, boxHeight, 12, 12);

        g2.setColor(UiTheme.TEXT);
        g2.drawString(toastMessage, x + (boxWidth - textWidth) / 2, y + 19);
    }

    public void loadMap() {
        try {
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

        System.out.println("Load map + collision OK");

        // Spawn many random items on the current screen so player can pick them up
        sceneManager.spawnRandomItems(120, worldWidth, worldHeight);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadParallax() {
    try {

        parallaxLayers = new BufferedImage[4];

        parallaxLayers[0] = ImageIO.read(
            getClass().getResourceAsStream(
                "/assets/background/parallax_layer1.png"
            )
        );

        parallaxLayers[1] = ImageIO.read(
            getClass().getResourceAsStream(
                "/assets/background/parallax_layer2.png"
            )
        );

        parallaxLayers[2] = ImageIO.read(
            getClass().getResourceAsStream(
                "/assets/background/parallax_layer3.png"
            )
        );

        parallaxLayers[3] = ImageIO.read(
            getClass().getResourceAsStream(
                "/assets/background/parallax_layer4.png"
            )
        );

        System.out.println("Loaded parallax layers");

    } catch (Exception e) {
        e.printStackTrace();
    }
}

private void drawParallax(Graphics2D g2) {

    double[] speeds = {
        0.05,
        0.2,
        0.4,
        0.8 // foreground layer, moves almost with the camera
    };

    for (int i = 0; i < parallaxLayers.length; i++) {

        BufferedImage layer = parallaxLayers[i];

        if (layer == null) continue;

        int x = (int)(-cameraX * speeds[i]);
        int y = (int)(-cameraY * speeds[i]);

        g2.drawImage(
            layer,
            x,
            y,
            null
        );
    }
}

    public GamePanel() {
        gameBuffer = new BufferedImage(
        SCREEN_WIDTH,
        SCREEN_HEIGHT,
        BufferedImage.TYPE_INT_ARGB
        );

        bufferG = gameBuffer.createGraphics();
        bufferG.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        setPanelSize();
        setBackground(new Color(20, 26, 38));
        setDoubleBuffered(true);
        setFocusable(true);
        setFocusTraversalKeys(
            KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
            Collections.emptySet()
        );
        setFocusTraversalKeys(
            KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
            Collections.emptySet()
        );

        addKeyListener(keyHandler);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        initPlayer();
        loadItemSprites();


        menu.update(mouseHandler);
    }



    private void setPanelSize() {
        Dimension size = new Dimension(
        SCREEN_WIDTH * WINDOW_SCALE,
        SCREEN_HEIGHT * WINDOW_SCALE
        );

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
        if (menu.isActive()) {
            menu.update(mouseHandler);
            return;
        }

        if (menu.isPaused()) {
            menu.update(mouseHandler);
            if (keyHandler.consumeEscapeJustPressed()) {
                menu.setPaused(false);
                requestFocusInWindow();
            }
            return;
        }

        menu.update(mouseHandler);

        handleHudMouseInput();

        if (keyHandler.consumeInventoryJustPressed()) {
            inventoryPanel.toggle();
            requestFocusInWindow();
        }

        if (inventoryPanel.isOpen()) {
            inventoryPanel.update(keyHandler, mouseHandler, this);
            return;
        }

        if (keyHandler.consumeEscapeJustPressed()) {
            menu.setPaused(true);
            inventoryPanel.close();
            requestFocusInWindow();
            return;
        }

        if (keyHandler.consumeQuickUseJustPressed(0)) {
            if (inventory.useItem(Item.ItemType.HP_POTION, player)) {
                showToast("Ban da dung HP Potion");
            }
        }
        if (keyHandler.consumeQuickUseJustPressed(1)) {
            if (inventory.useItem(Item.ItemType.MP_POTION, player)) {
                showToast("Ban da dung MP Potion");
            }
        }
        if (keyHandler.consumeQuickUseJustPressed(2)) {
            if (inventory.useItem(Item.ItemType.KEY, player)) {
                showToast("Ban da dung Key");
            }
        }

        player.update(keyHandler);
        player.updateResources();

        collisionHandler.move(
            player,
            player.getVelocityX(),
            player.getVelocityY()
        );

        handleItemPickup();
        updateCamera();
        handleShootingInput();
        updateBullets();

        if (leftShootCooldown > 0) {
            leftShootCooldown--;
        }
        if (rightShootCooldown > 0) {
            rightShootCooldown--;
        }
    }

    private void handleHudMouseInput() {
        if (!mouseHandler.isLeftJustPressed()) {
            return;
        }

        int[] coords = UiTheme.screenToBuffer(
            mouseHandler.getMouseX(),
            mouseHandler.getMouseY(),
            getWidth(),
            getHeight()
        );

        if (!Hud.containsBagButton(coords[0], coords[1])) {
            return;
        }

        mouseHandler.consumeLeftJustPressed();
        inventoryPanel.toggle();
        requestFocusInWindow();
    }

    private void handleItemPickup() {
        Screen currentScreen = sceneManager.getCurrentScreen();
        Rectangle playerBounds = new Rectangle(
            (int) player.getX(),
            (int) player.getY(),
            (int) player.getWidth(),
            (int) player.getHeight()
        );

        List<Item> items = new ArrayList<>(currentScreen.getItems());
        for (Item item : items) {
            if (item.isCollected()) {
                continue;
            }
            if (playerBounds.intersects(item.getBounds())) {
                inventory.addItem(item.getType(), 1);
                item.collect();
                currentScreen.removeItem(item);
                showToast("Ban da nhat " + inventory.getDescription(item.getType()).split(" — ")[0]);
            }
        }
    }

    public int getLeftShootCooldown() {
        return leftShootCooldown;
    }

    public int getLeftShootCooldownMax() {
        return LEFT_SHOOT_DELAY;
    }

    public int getRightShootCooldown() {
        return rightShootCooldown;
    }

    public int getRightShootCooldownMax() {
        return RIGHT_SHOOT_DELAY;
    }

 
    private static final double LOOK_AHEAD_DISTANCE = 120;
    private void updateCamera() {

        double halfW = SCREEN_WIDTH / 2;
        double halfH = SCREEN_HEIGHT  / 2;

        // ===== LOOK AHEAD =====

        double targetLookAhead = 0;

        if (player.getVelocityX() > 0) {
            targetLookAhead = LOOK_AHEAD_DISTANCE;
        }
        else if (player.getVelocityX() < 0) {
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

        // ===== CLAMP  =====
        cameraX = Math.max(0, cameraX);
        cameraY = Math.max(0, cameraY);

        cameraX = Math.min(cameraX, worldWidth - SCREEN_WIDTH );
        cameraY = Math.min(cameraY, worldHeight - SCREEN_HEIGHT);
}


// Handle shooting input and bullet spawning
    private void handleShootingInput() {
        if (mouseHandler.isLeftPressed() && leftShootCooldown <= 0) {
            if (player.spendMp(LEFT_SHOOT_MP_COST)) {
                spawnLaserShot();
                leftShootCooldown = LEFT_SHOOT_DELAY;
            }
        }

        if (mouseHandler.isRightPressed() && rightShootCooldown <= 0) {
            if (player.spendMp(RIGHT_SHOOT_MP_COST)) {
                spawnClusterShot();
                rightShootCooldown = RIGHT_SHOOT_DELAY;
            }
        }
    }

    private void spawnLaserShot() {
        double originX = player.getX() + 10;
        double originY = player.getY() + 15;

        //  convert mouse sang world space
        double worldMouseX = getMouseWorldX();
        double worldMouseY = getMouseWorldY();

        //  dùng world space để tính direction
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
            LASER_DAMAGE,
            Bullet.BulletType.LASER
        );
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

        for (int i = 0; i < CLUSTER_BULLET_COUNT; i++) {

            // ===== RANDOM SPREAD =====
            double randomAngle = Math.toRadians( (Math.random() - 0.5) * CLUSTER_SPREAD_DEGREES);

            double[] dir = rotateVector(
                baseDirectionX,
                baseDirectionY,
                randomAngle
            );

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
                CLUSTER_BULLET_DAMAGE,
                Bullet.BulletType.SHOTGUN
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
        int damage,
        Bullet.BulletType type
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
            damage,
            collisionMap,
            type
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

    private double getMouseWorldX() {
        double mouseX = (mouseHandler.getMouseX() - renderOffsetX)/ renderScale;
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
            // ===== PARALLAX =====
            drawParallax(bufferG); 

            // ===== CAMERA =====
            bufferG.translate(
                -(int)cameraX,
                -(int)cameraY
            );

            // ===== MAP =====
            if (mapImage != null) {
                bufferG.drawImage(mapImage, 0, 0, null);
            }
            
            // ===== ITEMS =====
            for (Item item : getCurrentItems()) {
                if (item.isCollected()) {
                    continue;
                }

                Rectangle r = item.getBounds();
                BufferedImage sprite = getItemSprite(item.getType());

                if (sprite != null) {
                    bufferG.drawImage(sprite, r.x, r.y, 64, 64, null);
                } else {
                    bufferG.setColor(item.getColor());

                    if (item.getShape() == Item.Shape.OVAL) {
                        bufferG.fillOval(r.x, r.y, r.width, r.height);
                    } else {
                        bufferG.fillRect(r.x, r.y, r.width, r.height);
                    }
                }
            }

            // ===== MONSTERS =====
            for (Monster monster : getCurrentMonsters()) {

                Rectangle r = monster.getBounds();

                bufferG.setColor(monster.getColor());
                bufferG.fillRect(r.x, r.y, r.width, r.height);

                
            }

            // ===== BULLETS =====
            for (Bullet bullet : bullets) {

         BufferedImage sprite = bullet.getSprite();

    if (sprite == null) {
        continue;
    }

    int width = sprite.getWidth();
    int height = sprite.getHeight();

    Graphics2D bulletG = (Graphics2D) bufferG.create();

int drawX =
    bullet.getRenderX() - (width / 2);

int drawY =
    bullet.getRenderY() - (height / 2);

bulletG.translate(
    drawX,
    drawY
);

    bulletG.rotate(
        bullet.getAngle(),
        width / 2.0,
        height / 2.0
    );

    bulletG.drawImage(
        sprite,
        0,
        0,
        null
    );

    bulletG.dispose();            
            }

            // ===== PLAYER =====
            player.draw(bufferG);

            // ===== HIDDEN LAYER =====
            if (hiddenImage != null) {
                bufferG.drawImage(hiddenImage, 0, 0, null);
            }

            // reset transform
            bufferG.setTransform(new java.awt.geom.AffineTransform());

            hud.render(bufferG);
            renderToast(bufferG);
            if (!menu.isPaused()) {
                inventoryPanel.render(bufferG);
            }
            if (menu.isPaused()) {
                menu.render(bufferG);
            }
        }

        // ===== DRAW BUFFER TO SCREEN =====

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
            RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        // ===== SCALE GIỮ TỈ LỆ =====
        double scaleX = (double) panelWidth / SCREEN_WIDTH;
        double scaleY = (double) panelHeight / SCREEN_HEIGHT;

        double scale = Math.min(scaleX, scaleY);

        // ===== SIZE SAU SCALE =====
        int renderWidth = (int)(SCREEN_WIDTH * scale);
        int renderHeight = (int)(SCREEN_HEIGHT * scale);

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
            null
        );
    }

    // CAWL AND BAWLS 
}