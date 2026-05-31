package goldenroad.game;

import java.awt.image.BufferedImage;

import goldenroad.entity.item.Inventory;
import goldenroad.entity.item.Item;
import goldenroad.entity.monster.Monster;
import goldenroad.entity.player.Player;
import goldenroad.entity.projectile.Bullet;
import goldenroad.map.CollisionHandler;
import goldenroad.map.CollisionMap;
import goldenroad.map.MapCatalog;
import goldenroad.map.MapDefinition;
import goldenroad.map.MapId;
import goldenroad.scene.SceneManager;
import goldenroad.scene.Screen;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import goldenroad.util.AssetLoader;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Graphics;

public class GameWorld {
    private MapId currentMapId = MapId.MAP_2;
    private CollisionMap collisionMap;
    private CollisionHandler collisionHandler;
    private BufferedImage mapImage;
    private BufferedImage hiddenImage;
    private int worldWidth;
    private int worldHeight;

    public MapId getCurrentMapId() {
        return currentMapId;
    }

    public void handleItemPickup(Player player, Inventory inventory, SceneManager sceneManager) {
        if (player == null || inventory == null || sceneManager == null) return;

        Screen currentScreen = sceneManager.getCurrentScreen();
        Rectangle playerBounds = new Rectangle(
            (int) player.getX(),
            (int) player.getY(),
            (int) player.getWidth(),
            (int) player.getHeight()
        );

        List<Item> items = new ArrayList<>(currentScreen.getItems());
        for (Item item : items) {
            if (item.isCollected()) continue;
            if (playerBounds.intersects(item.getBounds())) {
                inventory.addItem(item.getType(), 1);
                item.collect();
                currentScreen.removeItem(item);
            }
        }
    }

    public void updateMonsters(Player player, SceneManager sceneManager, List<Bullet> bullets) {
        if (sceneManager == null || collisionMap == null) return;

        for (Monster monster : sceneManager.getCurrentScreen().getMonsters()) {
            int damage = monster.update(player, collisionMap, bullets == null ? java.util.Collections.emptyList() : bullets);
            if (damage > 0) {
                player.takeDamage(damage);
            }
        }
    }

    public void updateBullets(List<Bullet> bullets, SceneManager sceneManager) {
        if (bullets == null || bullets.isEmpty() || sceneManager == null) return;

        Iterator<Bullet> it = bullets.iterator();
        while (it.hasNext()) {
            Bullet bullet = it.next();
            if (bullet.isDestroyed()) {
                it.remove();
                continue;
            }

            java.awt.Rectangle bounds = bullet.getBounds();
            if (isOutOfWorld(bounds) || collidesWithSolidBlock(bounds)) {
                it.remove();
                continue;
            }

            boolean hitMonster = false;
            Iterator<Monster> monsterIterator = sceneManager.getCurrentScreen().getMonsters().iterator();
            while (monsterIterator.hasNext()) {
                Monster monster = monsterIterator.next();
                if (bounds.intersects(monster.getBounds())) {
                    if (monster.takeDamage(bullet.getDamage())) {
                        monsterIterator.remove();
                    }
                    it.remove();
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

    private boolean collidesWithSolidBlock(java.awt.Rectangle bounds) {
        if (collisionMap == null) {
            return false;
        }

        return collisionMap.isAreaSolid(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    private boolean isOutOfWorld(java.awt.Rectangle bounds) {
        return bounds.x + bounds.width < 0
            || bounds.x > worldWidth
            || bounds.y + bounds.height < 0
            || bounds.y > worldHeight;
    }

    public void render(Graphics2D g, Player player, List<Bullet> bullets, SceneManager sceneManager, java.util.Map<Item.ItemType, BufferedImage> itemSprites) {
        if (g == null || sceneManager == null) return;

        // MAP
        if (mapImage != null) {
            g.drawImage(mapImage, 0, 0, null);
        }

        // ITEMS
        for (Item item : sceneManager.getCurrentScreen().getItems()) {
            if (item.isCollected()) continue;

            java.awt.Rectangle r = item.getBounds();
            BufferedImage sprite = itemSprites == null ? null : itemSprites.get(item.getType());

            if (sprite != null) {
                g.drawImage(sprite, r.x, r.y, 64, 64, null);
            } else {
                g.setColor(item.getColor());
                if (item.getShape() == Item.Shape.OVAL) {
                    g.fillOval(r.x, r.y, r.width, r.height);
                } else {
                    g.fillRect(r.x, r.y, r.width, r.height);
                }
            }
        }

        // MONSTERS
        for (Monster monster : sceneManager.getCurrentScreen().getMonsters()) {
            monster.draw(g);
        }

        // BULLETS
        if (bullets != null) {
            for (Bullet bullet : bullets) {
                bullet.render(g);
            }
        }

        // HIDDEN LAYER
        if (hiddenImage != null) {
            g.drawImage(hiddenImage, 0, 0, null);
        }
    }

    public CollisionMap getCollisionMap() {
        return collisionMap;
    }

    public CollisionHandler getCollisionHandler() {
        return collisionHandler;
    }

    public BufferedImage getMapImage() {
        return mapImage;
    }

    public BufferedImage getHiddenImage() {
        return hiddenImage;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public void loadCurrentMap(SceneManager sceneManager, Player player, boolean spawnInitialItems) {
        loadMap(currentMapId, sceneManager, player, spawnInitialItems);
    }

    public void switchMap(SceneManager sceneManager, Player player, boolean spawnInitialItems) {
        MapId nextMapId = currentMapId == MapId.MAP_1 ? MapId.MAP_2 : MapId.MAP_1;
        loadMap(nextMapId, sceneManager, player, spawnInitialItems);
    }

    private void loadMap(MapId mapId, SceneManager sceneManager, Player player, boolean spawnInitialItems) {
        currentMapId = mapId;
        applyMap(MapCatalog.get(mapId), sceneManager, player, spawnInitialItems);
    }

    private void applyMap(MapDefinition mapDefinition, SceneManager sceneManager, Player player, boolean spawnInitialItems) {
        mapImage = AssetLoader.loadImage(mapDefinition.getBackgroundPath());
        hiddenImage = AssetLoader.loadImage(mapDefinition.getHiddenPath());

        collisionMap = new CollisionMap();
        collisionMap.load(mapDefinition.getCollisionPath());
        collisionHandler = new CollisionHandler(collisionMap);

        worldWidth = mapDefinition.getWorldWidth();
        worldHeight = mapDefinition.getWorldHeight();

        if (player != null) {
            player.setX(clamp(mapDefinition.getSpawnX(), 0, Math.max(0, worldWidth - 1)));
            player.setY(clamp(mapDefinition.getSpawnY(), 0, Math.max(0, worldHeight - 1)));
            player.setVelocityY(0);
            player.setOnGround(true);
        }

        if (spawnInitialItems && sceneManager != null) {
            sceneManager.spawnRandomItems(120, worldWidth, worldHeight);
        }
    }

    private int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(value, max));
    }
}