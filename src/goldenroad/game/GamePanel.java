package goldenroad.game;

import goldenroad.audio.BackgroundMusicPlayer;
import goldenroad.audio.SoundEffectPlayer;
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
    private static final String LEFT_SHOT_SOUND = "/assets/audio/pistol-gun-1-shot.wav";
    private static final String LEFT_HOLD_SOUND = "/assets/audio/pistol-gun-multi-shot.wav";
    private static final String RIGHT_SHOT_SOUND = "/assets/audio/shotgun.WAV";
    private static final String MENU_CLICK_SOUND = "/assets/audio/back_003.wav";
    private static final String PLAYER_DEATH_SOUND = "/assets/audio/death.wav";
    private static final String ENEMY_DEATH_SOUND = "/assets/audio/enemy_death.wav";
    private static final String HP_HEAL_SOUND = "/assets/audio/heart_heal.wav";
    private static final String MP_HEAL_SOUND = "/assets/audio/mp_heal.wav";
    private static final String PLAYER_HURT_SOUND = "/assets/audio/player_hurt.wav";
    private static final String PAUSE_SOUND = "/assets/audio/pause.wav";

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
    private final BackgroundMusicPlayer backgroundMusic = new BackgroundMusicPlayer(settings.getVolume());
    private final SoundEffectPlayer soundEffects = new SoundEffectPlayer(settings.getVolume());
    private final Menu menu = new Menu(this, settings);
    private final GameOverlayRenderer overlayRenderer = new GameOverlayRenderer();
    private final GameInputController inputController;
    private final List<Bullet> bullets = new ArrayList<>();
    private final Inventory inventory = new Inventory();
    private Hud hud;
    private InventoryPanel inventoryPanel;


    private Thread gameThread;

    private Player player;
    private boolean leftShotDuringCurrentPress;

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
        soundEffects.preload(
            LEFT_SHOT_SOUND,
            LEFT_HOLD_SOUND,
            RIGHT_SHOT_SOUND,
            MENU_CLICK_SOUND,
            PLAYER_DEATH_SOUND,
            ENEMY_DEATH_SOUND,
            HP_HEAL_SOUND,
            MP_HEAL_SOUND,
            PLAYER_HURT_SOUND,
            PAUSE_SOUND
        );
        backgroundMusic.playLoop("/assets/audio/Menu.wav");
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
        inventory.clear();
        player.heal(10_000);
        player.restoreMp(10_000);
        loadMap(MapId.MAP_0);
    }

    public void continueGame() {
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
            playCurrentMapMusic();
            requestFocusInWindow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchMap() {
        world.switchMap(sceneManager, player, false, settings.getDifficulty());
        syncWorldStateFromGameWorld();
        saveCurrentMap(currentMapId);
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
        world.switchMap(sceneManager, player, true, settings.getDifficulty());
        syncWorldStateFromGameWorld();
        saveCurrentMap(currentMapId);
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
            soundEffects.play(ENEMY_DEATH_SOUND);
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
            stopContinuousWeaponSounds();
            return;
        }

        player.update(keyHandler);
        player.updateResources();
        int hpBeforeDamage = player.getHp();

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
        backgroundMusic.setVolume(settings.getVolume());
        soundEffects.setVolume(settings.getVolume());
    }

    public void playMenuClickSound() {
        soundEffects.play(MENU_CLICK_SOUND);
    }

    public void playPauseSound() {
        soundEffects.play(PAUSE_SOUND);
    }

    public void playItemUseSound(Item.ItemType type, ItemUseResult result) {
        if (type == null || result == null || !result.success()) {
            return;
        }

        switch (type) {
            case HP_POTION -> soundEffects.play(HP_HEAL_SOUND);
            case MP_POTION -> soundEffects.play(MP_HEAL_SOUND);
            case KEY -> {
            }
        }
    }

    private void playPlayerDamageSound(int hpBeforeDamage) {
        if (hpBeforeDamage <= player.getHp()) {
            return;
        }

        soundEffects.play(player.getHp() <= 0 ? PLAYER_DEATH_SOUND : PLAYER_HURT_SOUND);
    }

    private void playEnemyDeathSounds(int defeatedMonsterCount) {
        for (int i = 0; i < defeatedMonsterCount; i++) {
            soundEffects.play(ENEMY_DEATH_SOUND);
        }
    }

    private void playCurrentMapMusic() {
        backgroundMusic.playLoop(MapCatalog.get(currentMapId).getMusicPath());
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

            if (!leftBullets.isEmpty()) {
                if (leftShotDuringCurrentPress) {
                    soundEffects.playLoop(LEFT_HOLD_SOUND);
                } else {
                    soundEffects.play(LEFT_SHOT_SOUND);
                    leftShotDuringCurrentPress = true;
                }
            } else if (player.getAttack().getLeftCooldown() == 0) {
                soundEffects.stopLoop(LEFT_HOLD_SOUND);
            }

            for (goldenroad.entity.projectile.BulletSpec spec : leftBullets) {
                spawnBullet(spec.originX, spec.originY, spec.dirX, spec.dirY, spec.speed, spec.diameter, spec.color, spec.damage, spec.type);
            }
        } else {
            stopContinuousWeaponSounds();
        }

        if (mouseHandler.isRightPressed()) {
            List<goldenroad.entity.projectile.BulletSpec> rightBullets =
                player.getAttack().tryRightShoot(originX, originY, worldMouseX, worldMouseY);

            if (!rightBullets.isEmpty()) {
                soundEffects.play(RIGHT_SHOT_SOUND);
            }

            for (goldenroad.entity.projectile.BulletSpec spec : rightBullets) {
                spawnBullet(spec.originX, spec.originY, spec.dirX, spec.dirY, spec.speed, spec.diameter, spec.color, spec.damage, spec.type);
            }
        }
    }

    private void stopContinuousWeaponSounds() {
        soundEffects.stopLoop(LEFT_HOLD_SOUND);
        leftShotDuringCurrentPress = false;
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
