package goldenroad.game;

import goldenroad.entity.item.Inventory;
import goldenroad.entity.item.Item;
import goldenroad.entity.monster.Monster;
import goldenroad.entity.player.Player;
import goldenroad.entity.projectile.Bullet;
import goldenroad.entity.projectile.Bullet.BulletType;
import goldenroad.input.KeyHandler;
import goldenroad.input.MouseHandler;
import goldenroad.map.CollisionHandler;
import goldenroad.map.CollisionMap;
import goldenroad.map.MapCatalog;
import goldenroad.map.MapDefinition;
import goldenroad.map.MapId;
import goldenroad.scene.SceneManager;
import goldenroad.scene.Screen;
import goldenroad.scene.Menu;
import goldenroad.render.Camera;
import goldenroad.render.RenderSystem;
import goldenroad.game.GameInputController;
import goldenroad.ui.Hud;
import goldenroad.ui.GameOverlayRenderer;
import goldenroad.ui.InventoryPanel;
import goldenroad.ui.UiTheme;
import goldenroad.util.AssetLoader;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.util.Collections;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import javax.imageio.ImageIO;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.EnumMap;
import java.util.List;
import java.util.Random;
import java.util.Map;

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
    private final Camera camera = new Camera();
    
    //PARALLAX
    private BufferedImage[] parallaxLayers;
    private final RenderSystem renderSystem = new RenderSystem(SCREEN_WIDTH, SCREEN_HEIGHT);

    // MAP
    private MapId currentMapId = MapId.MAP_3;
    private CollisionMap collisionMap;
    private CollisionHandler collisionHandler;
    private BufferedImage mapImage,hiddenImage;
    private BufferedImage hpItemSprite;
    private BufferedImage mpItemSprite;
    private BufferedImage keyItemSprite;
    private final Map<Item.ItemType, BufferedImage> itemSprites = new EnumMap<>(Item.ItemType.class);

    private String toastMessage = null;
    private long toastExpireAtNanos = 0L;

    private boolean minimapVisible = true;

    int worldWidth;
    int worldHeight;

    // GUN + AIM
    private double renderScale;
    private int renderOffsetX;
    private int renderOffsetY;

    // player attack/cooldowns moved into PlayerAttack component
    // END OF PLAYER VARIABLES

    // INPUT HANDLERS, SCENE MANAGER, AND BULLET LIST
    private final KeyHandler keyHandler = new KeyHandler();
    private final MouseHandler mouseHandler = new MouseHandler();
    private final SceneManager sceneManager = new SceneManager();
    private final GameWorld world = new GameWorld();
    private final Menu menu = new Menu(this);
    private final GameOverlayRenderer overlayRenderer = new GameOverlayRenderer();
    private final GameInputController inputController;
    private final List<Bullet> bullets = new ArrayList<>();
    private final Inventory inventory = new Inventory();
    private Hud hud;
    private InventoryPanel inventoryPanel;


    private Thread gameThread;

    private Player player;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(new Color(20, 26, 38));
        setDoubleBuffered(true);
        setFocusable(true);
        addKeyListener(keyHandler);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        initPlayer();
        loadItemSprites();
        inputController = new GameInputController(keyHandler, mouseHandler, sceneManager, menu, inventory, inventoryPanel);
    }

    public void initPlayer() {
        player = new Player(400, 1995);
        player.update(keyHandler);
        hud = new Hud(this, player, inventory);
        inventoryPanel = new InventoryPanel(inventory, player);
    }

    private BufferedImage loadSprite(String resourcePath) {
        return AssetLoader.loadImage(resourcePath);
    }
    private void loadItemSprites() {
        hpItemSprite = loadSprite("/assets/item/hp.png");
        mpItemSprite = loadSprite("/assets/item/mp.png");
        keyItemSprite = loadSprite("/assets/item/key.png");

        itemSprites.put(Item.ItemType.HP_POTION, hpItemSprite);
        itemSprites.put(Item.ItemType.MP_POTION, mpItemSprite);
        itemSprites.put(Item.ItemType.KEY, keyItemSprite);
    }

    private BufferedImage getItemSprite(Item.ItemType type) {
        return switch (type) {
            case HP_POTION -> hpItemSprite;
            case MP_POTION -> mpItemSprite;
            case KEY -> keyItemSprite;
        };
    }
    private int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(value, max));
    }


    public void showToast(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        toastMessage = message;
        toastExpireAtNanos = System.nanoTime() + 2_000_000_000L;
    }

    public void loadMap() {
        try {
            world.loadCurrentMap(sceneManager, player, true);
            syncWorldStateFromGameWorld();
            camera.reset();
            player.getAttack().resetCooldowns();
            bullets.clear();
            System.out.println("Load map + collision OK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchMap() {
        world.switchMap(sceneManager, player, false);
        syncWorldStateFromGameWorld();
        camera.reset();
        player.getAttack().resetCooldowns();
        bullets.clear();
        menu.setPaused(false);
        inventoryPanel.close();
        showToast(currentMapId == MapId.MAP_1 ? "Da chuyen sang map 1" : "Da chuyen sang map 2");
        requestFocusInWindow();
    }

    public void toggleMinimap() {
        minimapVisible = !minimapVisible;
        showToast(minimapVisible ? "Minimap bat" : "Minimap tat");
        requestFocusInWindow();
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

        int x = (int)(-camera.getX() * speeds[i]);
        int y = (int)(-camera.getY() * speeds[i]);

        g2.drawImage(layer, x, y, null);
    }
}

    private void update() {
        if (inputController.update(this, player)) {
            return;
        }

        player.update(keyHandler);
        player.updateResources();

        collisionHandler.move(
            player,
            player.getVelocityX(),
            player.getVelocityY()
        );

        handleItemPickup();
        updateMonsters();
        camera.update(player, SCREEN_WIDTH, SCREEN_HEIGHT, world.getWorldWidth(), world.getWorldHeight());
        handleShootingInput();
        world.updateBullets(bullets, sceneManager);
    }

    private void handleItemPickup() {
        world.handleItemPickup(player, inventory, sceneManager);
    }

    private void updateMonsters() {
        world.updateMonsters(player, sceneManager, bullets);
    }

    private void syncWorldStateFromGameWorld() {
        currentMapId = world.getCurrentMapId();
        collisionMap = world.getCollisionMap();
        collisionHandler = world.getCollisionHandler();
        mapImage = world.getMapImage();
        hiddenImage = world.getHiddenImage();
        worldWidth = world.getWorldWidth();
        worldHeight = world.getWorldHeight();
    }

    public int getLeftShootCooldown() {
        return player.getAttack().getLeftCooldown();
    }

    public int getLeftShootCooldownMax() {
        return player.getAttack().getLeftCooldownMax();
    }

    public int getRightShootCooldown() {
        return player.getAttack().getRightCooldown();
    }

    public int getRightShootCooldownMax() {
        return player.getAttack().getRightCooldownMax();
    }


// Handle shooting input and bullet spawning
    private void handleShootingInput() {
        double originX = player.getX() + 10;
        double originY = player.getY() + 15;
        double worldMouseX = getMouseWorldX();
        double worldMouseY = getMouseWorldY();

        if (mouseHandler.isLeftPressed()) {
            for (goldenroad.entity.projectile.BulletSpec spec : player.getAttack().tryLeftShoot(originX, originY, worldMouseX, worldMouseY)) {
                spawnBullet(spec.originX, spec.originY, spec.dirX, spec.dirY, spec.speed, spec.diameter, spec.color, spec.damage, spec.type);
            }
        }

        if (mouseHandler.isRightPressed()) {
            for (goldenroad.entity.projectile.BulletSpec spec : player.getAttack().tryRightShoot(originX, originY, worldMouseX, worldMouseY)) {
                spawnBullet(spec.originX, spec.originY, spec.dirX, spec.dirY, spec.speed, spec.diameter, spec.color, spec.damage, spec.type);
            }
        }
    }

    // Old spawn helpers removed; shooting handled by `PlayerAttack` and `BulletSpec`.

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
            world.getCollisionMap(),
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
        return mouseX + camera.getX();
    }

    private double getMouseWorldY() {

        double mouseY = (mouseHandler.getMouseY() - renderOffsetY) / renderScale;
        return mouseY + camera.getY();
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

        Graphics2D bufferG = renderSystem.begin();

        if (menu.isActive()) {
            menu.render(bufferG);
        } else {
            renderScene(bufferG);
        }

        // ===== DRAW BUFFER TO SCREEN =====
        RenderSystem.Viewport viewport = renderSystem.end(g);
        renderScale = viewport.getScale();
        renderOffsetX = viewport.getOffsetX();
        renderOffsetY = viewport.getOffsetY();
    }

    private void renderScene(Graphics2D bufferG) {
        drawWorld(bufferG);
        drawHudAndOverlays(bufferG);
    }

    private void drawWorld(Graphics2D bufferG) {
        drawParallax(bufferG);

        bufferG.translate(
            -(int)camera.getX(),
            -(int)camera.getY()
        );

        world.render(bufferG, player, bullets, sceneManager, itemSprites);
        player.draw(bufferG);

        bufferG.setTransform(new java.awt.geom.AffineTransform());
    }

    private void drawHudAndOverlays(Graphics2D bufferG) {
        hud.render(bufferG);

        if (toastMessage != null && System.nanoTime() >= toastExpireAtNanos) {
            toastMessage = null;
        }

        overlayRenderer.renderToast(bufferG, toastMessage);

        if (!menu.isPaused()) {
            inventoryPanel.render(bufferG);
        }

        if (menu.isPaused()) {
            menu.render(bufferG);
        }

        if (!menu.isPaused() && minimapVisible) {
            overlayRenderer.renderMinimap(bufferG, world, player, camera.getX(), camera.getY(), SCREEN_WIDTH, SCREEN_HEIGHT);
        }
    }

    // CAWL AND BAWLS 

    // ----- Game loop -----
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
}
