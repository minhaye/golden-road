package goldenroad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import goldenroad.entity.Bullet;
import goldenroad.entity.Item;
import goldenroad.entity.Monster;
import goldenroad.scene.SceneManager;
import goldenroad.scene.ScreenData;

public class PlayScreen extends ScreenAdapter {
    public static final int DEFAULT_SCREEN_WIDTH = 1280;
    public static final int DEFAULT_SCREEN_HEIGHT = 720;

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
    private static final Color LASER_COLOR = color(255, 90, 80);

    private static final double CLUSTER_BULLET_SPEED = 10.5;
    private static final int CLUSTER_BULLET_DIAMETER = 7;
    private static final int CLUSTER_BULLET_DAMAGE = 1;
    private static final int CLUSTER_BULLET_COUNT = 6;
    private static final double CLUSTER_SPREAD_DEGREES = 22.0;
    private static final Color CLUSTER_COLOR = color(255, 235, 160);

    private static final int CAMERA_DEAD_ZONE_WIDTH = 220;
    private static final int CAMERA_DEAD_ZONE_HEIGHT = 140;
    private static final double FIXED_DELTA_SECONDS = 1.0 / TARGET_FPS;

    private final SceneManager sceneManager = new SceneManager();
    private final List<Bullet> bullets = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();

    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch spriteBatch;
    private final BitmapFont font;

    private double playerX = START_PLAYER_X;
    private double playerY = START_PLAYER_Y;
    private double velocityY = 0;
    private int jumpCount = 0;
    private boolean onGround = true;
    private double cameraX = 0;
    private double cameraY = 0;
    private boolean exitDoorVisible = false;

    private boolean previousJumpPressed;
    private boolean previousInteractPressed;
    private boolean previousLeftClick;
    private boolean previousRightClick;

    public PlayScreen() {
        camera = new OrthographicCamera();
        camera.setToOrtho(true, DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT);
        viewport = new FitViewport(DEFAULT_SCREEN_WIDTH, DEFAULT_SCREEN_HEIGHT, camera);
        viewport.apply(true);

        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.08f, 0.1f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update();
        draw();
    }

    private void update() {
        double currentMoveSpeed = isSprintPressed() ? MOVE_SPEED * SPRINT_MULTIPLIER : MOVE_SPEED;
        double moveX = 0;
        if (isLeftPressed() && !isRightPressed()) {
            moveX = -currentMoveSpeed;
        } else if (isRightPressed() && !isLeftPressed()) {
            moveX = currentMoveSpeed;
        }

        applyHorizontalMovement(moveX);

        if (consumeJumpJustPressed() && jumpCount < MAX_JUMPS) {
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
        if (consumeLeftClick()) {
            spawnLaserShot();
        }

        if (consumeRightClick()) {
            spawnClusterShot();
        }
    }

    private void spawnLaserShot() {
        double originX = playerX + (PLAYER_WIDTH / 2.0);
        double originY = playerY + (PLAYER_HEIGHT / 2.0);

        Vector3 worldMouse = getMouseWorld();
        double directionX = worldMouse.x - originX;
        double directionY = worldMouse.y - originY;

        spawnBullet(originX, originY, directionX, directionY, LASER_SPEED, LASER_DIAMETER, LASER_COLOR, LASER_DAMAGE);
    }

    private void spawnClusterShot() {
        double originX = playerX + (PLAYER_WIDTH / 2.0);
        double originY = playerY + (PLAYER_HEIGHT / 2.0);

        Vector3 worldMouse = getMouseWorld();
        double baseDirectionX = worldMouse.x - originX;
        double baseDirectionY = worldMouse.y - originY;

        if (baseDirectionX == 0 && baseDirectionY == 0) {
            baseDirectionX = 1;
        }

        double spreadStep = CLUSTER_BULLET_COUNT == 1 ? 0 : CLUSTER_SPREAD_DEGREES / (CLUSTER_BULLET_COUNT - 1);
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
        ScreenData currentScreen = sceneManager.getCurrentScreen();
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
                if (bulletBounds.overlaps(monster.getBounds())) {
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
        ScreenData currentScreen = sceneManager.getCurrentScreen();
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

            if (bulletBounds.overlaps(playerBounds)) {
                bulletIterator.remove();
            }
        }
    }

    private boolean collidesWithSolidBlock(Rectangle bounds) {
        for (Rectangle block : getCurrentSolidBlocks()) {
            if (bounds.overlaps(block)) {
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
        int viewportWidth = getViewportWidth();
        int viewportHeight = getViewportHeight();

        double playerCenterX = playerX + (PLAYER_WIDTH / 2.0);
        double playerCenterY = playerY + (PLAYER_HEIGHT / 2.0);

        double deadZoneLeft = cameraX + ((viewportWidth - CAMERA_DEAD_ZONE_WIDTH) / 2.0);
        double deadZoneRight = deadZoneLeft + CAMERA_DEAD_ZONE_WIDTH;
        if (playerCenterX < deadZoneLeft) {
            cameraX = playerCenterX - ((viewportWidth - CAMERA_DEAD_ZONE_WIDTH) / 2.0);
        } else if (playerCenterX > deadZoneRight) {
            cameraX = playerCenterX - ((viewportWidth + CAMERA_DEAD_ZONE_WIDTH) / 2.0);
        }

        double deadZoneTop = cameraY + ((viewportHeight - CAMERA_DEAD_ZONE_HEIGHT) / 2.0);
        double deadZoneBottom = deadZoneTop + CAMERA_DEAD_ZONE_HEIGHT;
        if (playerCenterY < deadZoneTop) {
            cameraY = playerCenterY - ((viewportHeight - CAMERA_DEAD_ZONE_HEIGHT) / 2.0);
        } else if (playerCenterY > deadZoneBottom) {
            cameraY = playerCenterY - ((viewportHeight + CAMERA_DEAD_ZONE_HEIGHT) / 2.0);
        }

        cameraX = clamp(cameraX, 0, Math.max(0, getCurrentWorldWidth() - viewportWidth));
        cameraY = clamp(cameraY, 0, Math.max(0, getCurrentWorldHeight() - viewportHeight));

        camera.position.set((float) (cameraX + viewportWidth / 2.0), (float) (cameraY + viewportHeight / 2.0), 0);
        camera.update();
    }

    private int getViewportWidth() {
        return Math.max(1, Math.round(viewport.getWorldWidth()));
    }

    private int getViewportHeight() {
        return Math.max(1, Math.round(viewport.getWorldHeight()));
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
            if (nextBounds.overlaps(block)) {
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
            if (nextBounds.overlaps(block)) {
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
        return new Rectangle((float) x, (float) y, PLAYER_WIDTH, PLAYER_HEIGHT);
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
                floorTopY = (int) block.y;
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
        if (!playerBounds.overlaps(getExitDoorBounds())) {
            return;
        }

        if (!consumeInteractJustPressed()) {
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

    private List<Rectangle> getCurrentSolidBlocks() {
        return sceneManager.getCurrentScreen().getSolidBlocks();
    }

    private List<Monster> getCurrentMonsters() {
        return sceneManager.getCurrentScreen().getMonsters();
    }

    private List<Item> getCurrentItems() {
        return sceneManager.getCurrentScreen().getItems();
    }

    private void draw() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(color(70, 110, 160));
        shapeRenderer.rect((float) cameraX, (float) cameraY, getViewportWidth(), getViewportHeight());

        ScreenData currentScreen = sceneManager.getCurrentScreen();
        if (currentScreen.hasTileMap()) {
            drawTileMap(currentScreen);
        } else {
            shapeRenderer.setColor(color(55, 45, 35));
            for (Rectangle block : getCurrentSolidBlocks()) {
                shapeRenderer.rect(block.x, block.y, block.width, block.height);
            }
        }

        for (Item item : getCurrentItems()) {
            Rectangle itemBounds = item.getBounds();
            shapeRenderer.setColor(item.getColor());
            if (item.getShape() == Item.Shape.OVAL) {
                shapeRenderer.ellipse(itemBounds.x, itemBounds.y, itemBounds.width, itemBounds.height);
            } else {
                shapeRenderer.rect(itemBounds.x, itemBounds.y, itemBounds.width, itemBounds.height);
            }
        }

        for (Monster monster : getCurrentMonsters()) {
            Rectangle monsterBounds = monster.getBounds();
            shapeRenderer.setColor(monster.getColor());
            drawMonster(monster, monsterBounds);
        }

        for (Bullet bullet : bullets) {
            shapeRenderer.setColor(bullet.getColor());
            shapeRenderer.circle(bullet.getRenderX() + (bullet.getDiameter() / 2f), bullet.getRenderY() + (bullet.getDiameter() / 2f), bullet.getDiameter() / 2f);
        }

        for (Bullet bullet : enemyBullets) {
            shapeRenderer.setColor(bullet.getColor());
            shapeRenderer.circle(bullet.getRenderX() + (bullet.getDiameter() / 2f), bullet.getRenderY() + (bullet.getDiameter() / 2f), bullet.getDiameter() / 2f);
        }

        shapeRenderer.setColor(color(230, 190, 70));
        shapeRenderer.rect((float) playerX, (float) playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        if (exitDoorVisible) {
            Rectangle doorBounds = getExitDoorBounds();

            shapeRenderer.setColor(color(40, 60, 90));
            shapeRenderer.rect(doorBounds.x, doorBounds.y, doorBounds.width, doorBounds.height);

            shapeRenderer.setColor(color(245, 215, 130));
            float knobX = doorBounds.x + doorBounds.width - 8;
            float knobY = doorBounds.y + (doorBounds.height / 2f);
            shapeRenderer.circle(knobX, knobY, 3.5f);
        }

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.setColor(new Color(1f, 1f, 1f, 0.22f));
        float deadZoneRenderX = (float) (cameraX + ((getViewportWidth() - CAMERA_DEAD_ZONE_WIDTH) / 2.0));
        float deadZoneRenderY = (float) (cameraY + ((getViewportHeight() - CAMERA_DEAD_ZONE_HEIGHT) / 2.0));
        shapeRenderer.rect(deadZoneRenderX, deadZoneRenderY, CAMERA_DEAD_ZONE_WIDTH, CAMERA_DEAD_ZONE_HEIGHT);

        if (exitDoorVisible) {
            Rectangle doorBounds = getExitDoorBounds();
            shapeRenderer.setColor(color(230, 245, 255));
            shapeRenderer.rect(doorBounds.x, doorBounds.y, doorBounds.width, doorBounds.height);
        }

        shapeRenderer.end();

        Rectangle playerBounds = getPlayerBounds(playerX, playerY);
        Rectangle doorBounds = getExitDoorBounds();
        if (exitDoorVisible && playerBounds.overlaps(doorBounds)) {
            spriteBatch.setProjectionMatrix(camera.combined);
            spriteBatch.begin();
            font.setColor(Color.WHITE);
            font.draw(spriteBatch, "Press E to enter", doorBounds.x - 22, doorBounds.y - 8);
            spriteBatch.end();
        }
    }

    private void drawTileMap(ScreenData screen) {
        if (screen.hasTiledTileMap()) {
            drawGidTileMap(screen);
            return;
        }

        drawLegacyTileMap(screen);
    }

    private void drawGidTileMap(ScreenData screen) {
        int[][] gids = screen.getTileGids();
        int tileSize = screen.getTileSize();

        if (gids == null || tileSize <= 0) {
            return;
        }

        for (int row = 0; row < gids.length; row++) {
            int[] rowData = gids[row];
            if (rowData == null) {
                continue;
            }

            for (int col = 0; col < rowData.length; col++) {
                int gid = rowData[col];
                if (gid <= 0) {
                    continue;
                }

                shapeRenderer.setColor(color(90, 90, 90));
                shapeRenderer.rect(col * tileSize, row * tileSize, tileSize, tileSize);
            }
        }
    }

    private void drawLegacyTileMap(ScreenData screen) {
        List<String> rows = screen.getTileRows();
        int tileSize = screen.getTileSize();

        for (int row = 0; row < rows.size(); row++) {
            String rowData = rows.get(row);
            for (int col = 0; col < rowData.length(); col++) {
                char tile = rowData.charAt(col);
                Color tileColor = getTileColor(tile);

                if (tileColor != null) {
                    shapeRenderer.setColor(tileColor);
                    shapeRenderer.rect(col * tileSize, row * tileSize, tileSize, tileSize);
                }
            }
        }
    }

    private Color getTileColor(char tile) {
        switch (tile) {
            case '#':
                return color(75, 70, 66);
            case '=':
                return color(118, 108, 96);
            case 'B':
                return color(120, 52, 58);
            case 'K':
                return color(244, 209, 90);
            case 'E':
                return color(78, 205, 190);
            default:
                return null;
        }
    }

    private void drawMonster(Monster monster, Rectangle bounds) {
        switch (monster.getRenderShape()) {
            case OVAL:
                shapeRenderer.ellipse(bounds.x, bounds.y, bounds.width, bounds.height);
                break;
            case TRIANGLE:
                shapeRenderer.triangle(
                    bounds.x + (bounds.width / 2f), bounds.y,
                    bounds.x, bounds.y + bounds.height,
                    bounds.x + bounds.width, bounds.y + bounds.height
                );
                break;
            default:
                shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
                break;
        }
    }

    private boolean isLeftPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
    }

    private boolean isRightPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
    }

    private boolean isSprintPressed() {
        return Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
    }

    private boolean consumeJumpJustPressed() {
        boolean jumpPressed = Gdx.input.isKeyPressed(Input.Keys.W)
            || Gdx.input.isKeyPressed(Input.Keys.UP)
            || Gdx.input.isKeyPressed(Input.Keys.SPACE);
        boolean justPressed = jumpPressed && !previousJumpPressed;
        previousJumpPressed = jumpPressed;
        return justPressed;
    }

    private boolean consumeInteractJustPressed() {
        boolean interactPressed = Gdx.input.isKeyPressed(Input.Keys.E);
        boolean justPressed = interactPressed && !previousInteractPressed;
        previousInteractPressed = interactPressed;
        return justPressed;
    }

    private boolean consumeLeftClick() {
        boolean leftPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean justPressed = leftPressed && !previousLeftClick;
        previousLeftClick = leftPressed;
        return justPressed;
    }

    private boolean consumeRightClick() {
        boolean rightPressed = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
        boolean justPressed = rightPressed && !previousRightClick;
        previousRightClick = rightPressed;
        return justPressed;
    }

    private Vector3 getMouseWorld() {
        Vector3 mouse = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mouse);
        return mouse;
    }

    private static Color color(int r, int g, int b) {
        return new Color(r / 255f, g / 255f, b / 255f, 1f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.setToOrtho(true, viewport.getWorldWidth(), viewport.getWorldHeight());
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}
