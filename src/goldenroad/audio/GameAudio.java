package goldenroad.audio;

import goldenroad.entity.item.Item;
import goldenroad.entity.item.ItemUseResult;
import goldenroad.map.MapCatalog;
import goldenroad.map.MapId;
import goldenroad.settings.GameSettings;

public class GameAudio {
    private static final String LEFT_SHOT_SOUND = "/assets/audio/pistol-gun-1-shot.wav";
    private static final String RIGHT_SHOT_SOUND = "/assets/audio/shotgun.WAV";
    private static final String MENU_CLICK_SOUND = "/assets/audio/back_003.wav";
    private static final String PLAYER_DEATH_SOUND = "/assets/audio/death.wav";
    private static final String ENEMY_DEATH_SOUND = "/assets/audio/enemy_death.wav";
    private static final String HP_HEAL_SOUND = "/assets/audio/heart_heal.wav";
    private static final String MP_HEAL_SOUND = "/assets/audio/mp_heal.wav";
    private static final String PLAYER_HURT_SOUND = "/assets/audio/player_hurt.wav";
    private static final String PAUSE_SOUND = "/assets/audio/pause.wav";
    private static final String PICK_ITEM_SOUND = "/assets/audio/pick_item.wav";
    private static final String MENU_MUSIC = "/assets/audio/Menu.wav";

    private final BackgroundMusicPlayer backgroundMusic;
    private final SoundEffectPlayer soundEffects;

    public GameAudio(GameSettings settings) {
        this.backgroundMusic = new BackgroundMusicPlayer(settings.getVolume());
        this.soundEffects = new SoundEffectPlayer(settings.getVolume());
        soundEffects.preload(
            LEFT_SHOT_SOUND,
            RIGHT_SHOT_SOUND,
            MENU_CLICK_SOUND,
            PLAYER_DEATH_SOUND,
            ENEMY_DEATH_SOUND,
            HP_HEAL_SOUND,
            MP_HEAL_SOUND,
            PLAYER_HURT_SOUND,
            PAUSE_SOUND,
            PICK_ITEM_SOUND
        );
    }

    public void applyVolume(int volume) {
        backgroundMusic.setVolume(volume);
        soundEffects.setVolume(volume);
    }

    public void playLeftShot() {
        soundEffects.play(LEFT_SHOT_SOUND);
    }

    public void playRightShot() {
        soundEffects.play(RIGHT_SHOT_SOUND);
    }

    public void playMenuClick() {
        soundEffects.play(MENU_CLICK_SOUND);
    }

    public void playPause() {
        soundEffects.play(PAUSE_SOUND);
    }

    public void playItemUse(Item.ItemType type, ItemUseResult result) {
        if (result == null || !result.success()) {
            return;
        }

        if (type == Item.ItemType.HP_POTION) {
            soundEffects.play(HP_HEAL_SOUND);
        } else if (type == Item.ItemType.MP_POTION) {
            soundEffects.play(MP_HEAL_SOUND);
        }
    }

    public void playPlayerDamage(int damageTaken) {
        if (damageTaken > 0) {
            soundEffects.play(PLAYER_HURT_SOUND);
        }
    }

    public void playPlayerDeath() {
        soundEffects.play(PLAYER_DEATH_SOUND);
    }

    public void playEnemyDeaths(int defeatedMonsterCount) {
        for (int i = 0; i < defeatedMonsterCount; i++) {
            soundEffects.play(ENEMY_DEATH_SOUND);
        }
    }

    public void playEnemyDeath() {
        soundEffects.play(ENEMY_DEATH_SOUND);
    }

    public void playItemPickups(int collectedItemCount) {
        for (int i = 0; i < collectedItemCount; i++) {
            soundEffects.play(PICK_ITEM_SOUND);
        }
    }

    public void playMapMusic(MapId mapId) {
        backgroundMusic.playLoop(MapCatalog.get(mapId).getMusicPath());
    }

    public void playMenuMusic() {
        backgroundMusic.playLoop(MENU_MUSIC);
    }
}
