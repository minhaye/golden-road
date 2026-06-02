package goldenroad.game;

import goldenroad.entity.item.Inventory;
import goldenroad.entity.item.Item;
import goldenroad.entity.item.ItemUseContext;
import goldenroad.entity.item.ItemUseResult;
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
import goldenroad.settings.GameSettings;
import goldenroad.settings.SettingsStore;
import goldenroad.render.Camera;
import goldenroad.render.RenderSystem;
import goldenroad.game.GameInputController;
import goldenroad.ui.Hud;
import goldenroad.ui.GameOverlayRenderer;
import goldenroad.ui.InventoryPanel;
import goldenroad.ui.UiTheme;
import goldenroad.util.AssetLoader;


import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import javax.imageio.ImageIO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private static final Path SAVE_FILE = Paths.get(System.getProperty("user.home"), ".golden-road-save");
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
    private final GameSettings settings = SettingsStore.load();
    public final Menu menu = new Menu(this, settings);
    private final GameOverlayRenderer overlayRenderer = new GameOverlayRenderer();
    private final GameInputController inputController;
    private final List<Bullet> bullets = new ArrayList<>();
    private final Inventory inventory = new Inventory();
    private Hud hud;
    private InventoryPanel inventoryPanel;
    private boolean gameOver = false;
    private boolean victory = false;
    private java.awt.Rectangle gameOverRestartButton = new java.awt.Rectangle((SCREEN_WIDTH / 2) - 110, 170, 220, 44);
    private java.awt.Rectangle gameOverReturnButton = new java.awt.Rectangle((SCREEN_WIDTH / 2) - 110, 220, 220, 44);
    private java.awt.Rectangle gameOverExitButton = new java.awt.Rectangle((SCREEN_WIDTH / 2) - 110, 270, 220, 44);
    private java.awt.Rectangle victoryReturnButton = new java.awt.Rectangle((SCREEN_WIDTH / 2) - 110, 220, 220, 44);
    private java.awt.Rectangle victoryExitButton = new java.awt.Rectangle((SCREEN_WIDTH / 2) - 110, 280, 220, 44);


    private Thread gameThread;

    private Player player;

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        setBackground(new Color(20, 26, 38));
        setDoubleBuffered(true);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(keyHandler);
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        initPlayer();
        loadItemSprites();
        loadCustomCursor();
        inputController = new GameInputController(keyHandler, mouseHandler, sceneManager, menu, inventory, inventoryPanel);
    }

    public void initPlayer() {
        player = new Player(400, 1995);
        player.update(keyHandler);
        hud = new Hud(this, player, inventory);
        inventoryPanel = new InventoryPanel(inventory, player);
        inventory.setUseContext(new ItemUseContext() {
            @Override
            public Player player() {
                return player;
            }

            @Override
            public Inventory inventory() {
                return inventory;
            }

            @Override
            public boolean isCurrentMapClear() {
                return sceneManager.isCurrentMapClear();
            }

            @Override
            public MapId getCurrentMapId() {
                return world.getCurrentMapId();
            }

            @Override
            public void advanceToNextMap() {
                advanceMapWithSpawn();
            }
        });
    }

    private BufferedImage loadSprite(String resourcePath) {
        return AssetLoader.loadImage(resourcePath);
    }

    private void loadCustomCursor() {
        try {
            BufferedImage cursorImage = loadSprite("/assets/crosshair/crosshair.png");
            if (cursorImage == null) {
                return;
            }

            Point hotspot = new Point(cursorImage.getWidth() / 2, cursorImage.getHeight() / 2);
            Cursor crosshairCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, hotspot, "Crosshair");
            setCursor(crosshairCursor);
             System.out.println("crosshair OK");
        } catch (Exception e) {
            // ignore if custom cursor cannot be created
        }
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
            victory = false;
            world.loadCurrentMap(sceneManager, player, true, settings.getDifficulty());
            syncWorldStateFromGameWorld();
            camera.reset();
            player.getAttack().resetCooldowns();
            bullets.clear();
            System.out.println("Load map + collision OK");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startNewGame() {
        victory = false;
        inventory.clear();
        player.heal(10_000);
        player.restoreMp(10_000);
        loadMap(MapId.MAP_0);
    }

    public void continueGame() {
        victory = false;
        loadMap(loadSavedMap());
    }

    private void loadMap(MapId mapId) {
        try {
            world.loadMap(mapId, sceneManager, player, true, settings.getDifficulty());
            syncWorldStateFromGameWorld();
            saveCurrentMap(currentMapId);
            camera.reset();
            player.getAttack().resetCooldowns();
            bullets.clear();
            menu.setPaused(false);
            inventoryPanel.close();
            requestFocusInWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchMap() {
        victory = false;
        world.switchMap(sceneManager, player, false, settings.getDifficulty());
        syncWorldStateFromGameWorld();
        saveCurrentMap(currentMapId);
        camera.reset();
        player.getAttack().resetCooldowns();
        bullets.clear();
        menu.setPaused(false);
        inventoryPanel.close();
        showToast("Da chuyen sang " + currentMapId.displayName().toLowerCase());
        requestFocusInWindow();
    }

    public void advanceMapWithSpawn() {
        if (currentMapId == MapId.MAP_3) {
            victory = true;
            bullets.clear();
            menu.setPaused(false);
            inventoryPanel.close();
            requestFocusInWindow();
            return;
        }

        victory = false;
        world.switchMap(sceneManager, player, true, settings.getDifficulty());
        syncWorldStateFromGameWorld();
        saveCurrentMap(currentMapId);
        camera.reset();
        player.getAttack().resetCooldowns();
        bullets.clear();
        menu.setPaused(false);
        inventoryPanel.close();
        requestFocusInWindow();
    }

    public void killAllMonstersOnCurrentMap() {
        sceneManager.killAllMonstersOnCurrentMap();
        showToast("Da tieu diet tat ca quai (cheat)");
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
        if (gameOver) {
            handleGameOverInput();
            return;
        }
        if (victory) {
            handleVictoryInput();
            return;
        }
        if (inputController.update(this, player)) {
            return;
        }

        player.update(keyHandler);
        player.updateResources();

        if (player.getHp() <= 0 && !gameOver) {
            gameOver = true;
            // stop normal updates; allow the game over overlay to handle input
            bullets.clear();
            requestFocusInWindow();
            return;
        }

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
        world.handleItemPickup(player, inventory, sceneManager, this::showToast);
    }

    private void updateMonsters() {
        world.updateMonsters(player, sceneManager, bullets, settings.getDifficulty());
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void saveSettings() {
        SettingsStore.save(settings);
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

    public int getAliveMonsterCount() {
        try {
            java.util.List<goldenroad.entity.monster.Monster> monsters = sceneManager.getCurrentScreen().getMonsters();
            if (monsters == null) return 0;
            int alive = 0;
            for (goldenroad.entity.monster.Monster m : monsters) {
                if (m != null && !m.isDead()) alive++;
            }
            return alive;
        } catch (Exception e) {
            return 0;
        }
    }


// Handle shooting input and bullet spawning
    private void handleShootingInput() {
        if (menu.isActive() || menu.isPaused() || inventoryPanel.isOpen()) {
            return;
        }

        double worldMouseX = getMouseWorldX();
        double worldMouseY = getMouseWorldY();
        java.awt.geom.Point2D.Double gunCenter = player.getGunCenter(worldMouseX, worldMouseY);
        double originX = gunCenter.x;
        double originY = gunCenter.y;

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
        double scale = renderScale <= 0 ? 1.0 : renderScale;
        double mouseX = (mouseHandler.getMouseX() - renderOffsetX)/ scale;
        return mouseX + camera.getX();
    }

    private double getMouseWorldY() {

        double scale = renderScale <= 0 ? 1.0 : renderScale;
        double mouseY = (mouseHandler.getMouseY() - renderOffsetY) / scale;
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
        boolean aiming = !menu.isActive()
            && !menu.isPaused()
            && !inventoryPanel.isOpen()
            && (mouseHandler.isLeftPressed() || mouseHandler.isRightPressed());
        player.draw(bufferG, aiming, getMouseWorldX(), getMouseWorldY());
        drawHiddenLayer(bufferG);

        bufferG.setTransform(new java.awt.geom.AffineTransform());
    }

    private void drawHiddenLayer(Graphics2D bufferG) {
        if (hiddenImage == null) {
            return;
        }

        Composite previousComposite = bufferG.getComposite();
        bufferG.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        bufferG.drawImage(hiddenImage, 0, 0, null);
        bufferG.setComposite(previousComposite);
    }

    private void drawHudAndOverlays(Graphics2D bufferG) {
        hud.render(bufferG);

        if (toastMessage != null && System.nanoTime() >= toastExpireAtNanos) {
            toastMessage = null;
        }

        overlayRenderer.renderToast(bufferG, toastMessage);

        if (gameOver) {
            drawGameOver(bufferG);
            return;
        }

        if (victory) {
            drawVictoryScreen(bufferG);
            return;
        }

        if (!menu.isPaused()) {
            inventoryPanel.render(bufferG);
        }

        if (menu.isPaused()) {
            menu.render(bufferG);
        }

        if (!menu.isPaused() && minimapVisible) {
            overlayRenderer.renderMinimap(bufferG, world, sceneManager, player, camera.getX(), camera.getY(), SCREEN_WIDTH, SCREEN_HEIGHT);
        }
    }

    private void drawGameOver(Graphics2D g) {
        g.setColor(new Color(8, 10, 12, 220));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 48));
        g.setColor(new Color(100, 228, 250));
        String title = "CONSTRUCT DOWN";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (SCREEN_WIDTH - tw) / 2, 140);

        // draw restart button
        drawGameOverButton(g, gameOverRestartButton, "Restart");
        // draw return button
        drawGameOverButton(g, gameOverReturnButton, "Return to Menu");
        // draw exit button
        drawGameOverButton(g, gameOverExitButton, "Exit Game");
    }

    private void drawVictoryScreen(Graphics2D g) {
        g.setColor(new Color(8, 10, 12, 220));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 52));
        g.setColor(new Color(190, 240, 160));
        String title = "YOU WIN!";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (SCREEN_WIDTH - tw) / 2, 140);

        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 20));
        g.setColor(new Color(220, 230, 240));
        String message = "Congratulation! Ban da hoan thanh man cuoi cung.";
        int mw = g.getFontMetrics().stringWidth(message);
        g.drawString(message, (SCREEN_WIDTH - mw) / 2, 190);

        drawGameOverButton(g, victoryReturnButton, "Return to Menu");
        drawGameOverButton(g, victoryExitButton, "Exit Game");
    }

    private void drawGameOverButton(Graphics2D g, java.awt.Rectangle btn, String label) {
        g.setColor(new Color(40, 45, 60));
        g.fillRoundRect(btn.x, btn.y, btn.width, btn.height, 8, 8);
        g.setColor(new Color(185, 210, 255));
        g.drawRoundRect(btn.x, btn.y, btn.width, btn.height, 8, 8);
        g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 20));
        int lw = g.getFontMetrics().stringWidth(label);
        int lx = btn.x + (btn.width - lw) / 2;
        int ly = btn.y + (btn.height / 2) + 7;
        g.setColor(new Color(230, 230, 240));
        g.drawString(label, lx, ly);
    }

    private void handleGameOverInput() {
        if (!mouseHandler.isLeftJustPressed()) return;
        if (!mouseHandler.consumeLeftJustPressed()) return;

        double scale = renderScale <= 0 ? 1.0 : renderScale;
        int mx = (int) ((mouseHandler.getMouseX() - renderOffsetX) / scale);
        int my = (int) ((mouseHandler.getMouseY() - renderOffsetY) / scale);

        if (gameOverRestartButton.contains(mx, my)) {
            // restart current map
            restartMap();
        } else if (gameOverReturnButton.contains(mx, my)) {
            // open main menu
            gameOver = false;
            player.heal(10_000);
            player.restoreMp(10_000);
            bullets.clear();
            menu.open();
        } else if (gameOverExitButton.contains(mx, my)) {
            // exit game
            System.exit(0);
        }
    }

    private void handleVictoryInput() {
        if (!mouseHandler.isLeftJustPressed()) return;
        if (!mouseHandler.consumeLeftJustPressed()) return;

        double scale = renderScale <= 0 ? 1.0 : renderScale;
        int mx = (int) ((mouseHandler.getMouseX() - renderOffsetX) / scale);
        int my = (int) ((mouseHandler.getMouseY() - renderOffsetY) / scale);

        if (victoryReturnButton.contains(mx, my)) {
            victory = false;
            player.heal(10_000);
            player.restoreMp(10_000);
            bullets.clear();
            menu.open();
        } else if (victoryExitButton.contains(mx, my)) {
            System.exit(0);
        }
    }

    private void restartMap() {
        try {
            gameOver = false;
            player.heal(10_000);
            player.restoreMp(10_000);
            world.loadCurrentMap(sceneManager, player, true, settings.getDifficulty());
            syncWorldStateFromGameWorld();
            camera.reset();
            player.getAttack().resetCooldowns();
            bullets.clear();
            inventoryPanel.close();
            requestFocusInWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restartCurrentMap() {
        if (menu.isPaused()) {
            menu.setPaused(false);
        }
        restartMap();
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

    private MapId loadSavedMap() {
        try {
            if (Files.exists(SAVE_FILE)) {
                List<String> lines = Files.readAllLines(SAVE_FILE, StandardCharsets.UTF_8);
                if (!lines.isEmpty()) {
                    return MapId.valueOf(lines.get(0).trim());
                }
            }
        } catch (Exception e) {
            // fall back to the current in-memory/default map
        }

        return currentMapId;
    }

    private void saveCurrentMap(MapId mapId) {
        if (mapId == null) {
            return;
        }

        try {
            Files.write(SAVE_FILE, List.of(mapId.name()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // ignore save failures so the game keeps running
        }
    }
}
