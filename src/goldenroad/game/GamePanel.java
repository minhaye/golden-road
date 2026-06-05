package goldenroad.game;

import goldenroad.audio.GameAudio;
import goldenroad.entity.item.Inventory;
import goldenroad.entity.item.Item;
import goldenroad.entity.item.ItemUseContext;
import goldenroad.entity.item.ItemUseResult;
import goldenroad.entity.player.Player;
import goldenroad.entity.projectile.Bullet;
import goldenroad.input.KeyHandler;
import goldenroad.input.MouseHandler;
import goldenroad.map.CollisionHandler;
import goldenroad.map.CollisionMap;
import goldenroad.map.MapId;
import goldenroad.scene.SceneManager;
import goldenroad.scene.Menu;
import goldenroad.settings.GameSettings;
import goldenroad.settings.SettingsStore;
import goldenroad.settings.MapProgressStore;
import goldenroad.render.Camera;
import goldenroad.render.RenderSystem;
import goldenroad.render.ParallaxRenderer;
import goldenroad.game.GameInputController;
import goldenroad.ui.Hud;
import goldenroad.ui.GameOverlayRenderer;
import goldenroad.ui.ToastManager;
import goldenroad.ui.EndScreenOverlay;
import goldenroad.ui.EndScreenOverlay.EndScreenAction;
import goldenroad.ui.InventoryPanel;
import goldenroad.util.AssetLoader;


import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;


import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
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
    
    private final ParallaxRenderer parallaxRenderer = new ParallaxRenderer();
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

    private final GameAudio gameAudio = new GameAudio(settings);
    private final MapProgressStore progressStore = new MapProgressStore();
    public final Menu menu = new Menu(this, settings);
  
    private final GameOverlayRenderer overlayRenderer = new GameOverlayRenderer();
    private final ToastManager toastManager = new ToastManager();
    private final EndScreenOverlay endScreenOverlay = new EndScreenOverlay();
    private final GameInputController inputController;
    private final List<Bullet> bullets = new ArrayList<>();
    private final Inventory inventory = new Inventory();
    private Hud hud;
    private InventoryPanel inventoryPanel;
    private boolean gameOver = false;
    private boolean victory = false;


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
        gameAudio.playMenuMusic();
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


    public void showToast(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        toastManager.show(message);
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
        loadMap(progressStore.load(currentMapId));
    }

    private void loadMap(MapId mapId) {
        try {
            world.loadMap(mapId, sceneManager, player, true, settings.getDifficulty());
            syncWorldStateFromGameWorld();
            progressStore.save(currentMapId);
            camera.reset();
            player.getAttack().resetCooldowns();
            bullets.clear();
            menu.setPaused(false);
            inventoryPanel.close();
            playCurrentMapMusic();
            requestFocusInWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchMap() {
        victory = false;
        world.switchMap(sceneManager, player, false, settings.getDifficulty());
        syncWorldStateFromGameWorld();
        progressStore.save(currentMapId);
        camera.reset();
        player.getAttack().resetCooldowns();
        bullets.clear();
        menu.setPaused(false);
        inventoryPanel.close();
        playCurrentMapMusic();
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
        progressStore.save(currentMapId);
        camera.reset();
        player.getAttack().resetCooldowns();
        bullets.clear();
        menu.setPaused(false);
        inventoryPanel.close();
        playCurrentMapMusic();
        requestFocusInWindow();
    }

    public void killAllMonstersOnCurrentMap() {
        if (sceneManager.killAllMonstersOnCurrentMap() > 0) {
            gameAudio.playEnemyDeath();
        }
        showToast("Da tieu diet tat ca quai (cheat)");
        requestFocusInWindow();
    }

    public void toggleMinimap() {
        minimapVisible = !minimapVisible;
        showToast(minimapVisible ? "Minimap bat" : "Minimap tat");
        requestFocusInWindow();
    }

    public void loadParallax() {
        parallaxRenderer.load();
    }

    private void drawParallax(Graphics2D g2) {
        parallaxRenderer.render(g2, camera);
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
            stopContinuousWeaponSounds();
            return;
        }

        player.update(keyHandler);
        player.updateResources();
        int hpBeforeDamage = player.getHp();

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
        playPlayerDamageSound(hpBeforeDamage);
        camera.update(player, SCREEN_WIDTH, SCREEN_HEIGHT, world.getWorldWidth(), world.getWorldHeight());
        handleShootingInput();
        playEnemyDeathSounds(world.updateBullets(bullets, sceneManager));
    }

    private void handleItemPickup() {
        playItemPickupSounds(world.handleItemPickup(player, inventory, sceneManager, this::showToast));
    }

    private void updateMonsters() {
        world.updateMonsters(player, sceneManager, bullets, settings.getDifficulty());
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void saveSettings() {
        SettingsStore.save(settings);
        gameAudio.applyVolume(settings.getVolume());
    }

    public void playMenuClickSound() {
        gameAudio.playMenuClick();
    }

    public void playPauseSound() {
        gameAudio.playPause();
    }

    public void playItemUseSound(Item.ItemType type, ItemUseResult result) {
        gameAudio.playItemUse(type, result);
    }

    private void playPlayerDamageSound(int hpBeforeDamage) {
        if (hpBeforeDamage <= player.getHp()) {
            return;
        }

        if (player.getHp() <= 0) {
            gameAudio.playPlayerDeath();
        } else {
            gameAudio.playPlayerDamage(hpBeforeDamage - player.getHp());
        }
    }

    private void playEnemyDeathSounds(int defeatedMonsterCount) {
        gameAudio.playEnemyDeaths(defeatedMonsterCount);
    }

    private void playItemPickupSounds(int collectedItemCount) {
        gameAudio.playItemPickups(collectedItemCount);
    }

    private void playCurrentMapMusic() {
        gameAudio.playMapMusic(currentMapId);
    }

    public void playMenuMusic() {
        gameAudio.playMenuMusic();
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
            stopContinuousWeaponSounds();
            return;
        }

        double worldMouseX = getMouseWorldX();
        double worldMouseY = getMouseWorldY();
        java.awt.geom.Point2D.Double gunCenter = player.getGunCenter(worldMouseX, worldMouseY);
        double originX = gunCenter.x;
        double originY = gunCenter.y;

        if (mouseHandler.isLeftPressed()) {
            List<goldenroad.entity.projectile.BulletSpec> leftBullets =
                player.getAttack().tryLeftShoot(originX, originY, worldMouseX, worldMouseY);

            for (goldenroad.entity.projectile.BulletSpec spec : leftBullets) {
                gameAudio.playLeftShot();
                spawnBullet(spec.originX, spec.originY, spec.dirX, spec.dirY, spec.speed, spec.diameter, spec.color, spec.damage, spec.type);
            }
        } else {
            stopContinuousWeaponSounds();
        }

        if (mouseHandler.isRightPressed()) {
            List<goldenroad.entity.projectile.BulletSpec> rightBullets =
                player.getAttack().tryRightShoot(originX, originY, worldMouseX, worldMouseY);

            if (!rightBullets.isEmpty()) {
                gameAudio.playRightShot();
            }

            for (goldenroad.entity.projectile.BulletSpec spec : rightBullets) {
                spawnBullet(spec.originX, spec.originY, spec.dirX, spec.dirY, spec.speed, spec.diameter, spec.color, spec.damage, spec.type);
            }
        }
    }

    private void stopContinuousWeaponSounds() {
        // No continuous left-hold audio needed; each left click or hold bullet plays the shot sound.
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

        overlayRenderer.renderToast(bufferG, toastManager.currentMessage());

        if (gameOver) {
            endScreenOverlay.renderGameOver(bufferG);
            return;
        }

        if (victory) {
            endScreenOverlay.renderVictory(bufferG);
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


    private void handleGameOverInput() {
        if (!mouseHandler.isLeftJustPressed()) return;
        if (!mouseHandler.consumeLeftJustPressed()) return;

        EndScreenAction action = endScreenOverlay.gameOverActionAt(getScreenMouseX(), getScreenMouseY());
        if (action == EndScreenAction.RESTART) {
            restartMap();
        } else if (action == EndScreenAction.RETURN_TO_MENU) {
            gameOver = false;
            returnToMenuFromEndScreen();
        } else if (action == EndScreenAction.EXIT) {
            System.exit(0);
        }
    }

    private void handleVictoryInput() {
        if (!mouseHandler.isLeftJustPressed()) return;
        if (!mouseHandler.consumeLeftJustPressed()) return;

        EndScreenAction action = endScreenOverlay.victoryActionAt(getScreenMouseX(), getScreenMouseY());
        if (action == EndScreenAction.RETURN_TO_MENU) {
            victory = false;
            returnToMenuFromEndScreen();
        } else if (action == EndScreenAction.EXIT) {
            System.exit(0);
        }
    }

    private void returnToMenuFromEndScreen() {
        player.heal(10_000);
        player.restoreMp(10_000);
        bullets.clear();
        menu.open();
    }

    private int getScreenMouseX() {
        double scale = renderScale <= 0 ? 1.0 : renderScale;
        return (int) ((mouseHandler.getMouseX() - renderOffsetX) / scale);
    }

    private int getScreenMouseY() {
        double scale = renderScale <= 0 ? 1.0 : renderScale;
        return (int) ((mouseHandler.getMouseY() - renderOffsetY) / scale);
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

}
