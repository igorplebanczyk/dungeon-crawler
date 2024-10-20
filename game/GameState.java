package game;

import game.object.Dungeon;
import game.ui.Message;

public class GameState {
    private int level;
    private boolean paused = false;
    private boolean movementInProgress = false;
    private static boolean bulldozerMode = false;
    private Dungeon currentDungeon;
    private Message message;

    public GameState() {
        this.level = 1;
    }

    public int getLevel() {
        return this.level;
    }

    public void incLevel() {
        this.level++;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void togglePause() {
        this.paused = !this.paused;
    }

    public boolean isMovementInProgress() {
        return movementInProgress;
    }

    public void setMovementInProgress(boolean movementInProgress) {
        this.movementInProgress = movementInProgress;
    }

    public static boolean isBulldozerMode() {
        return bulldozerMode;
    }

    public static void setBulldozerMode(boolean mode) {
        bulldozerMode = mode;
    }

    public Dungeon getCurrentDungeon() {
        return this.currentDungeon;
    }

    public void setCurrentDungeon(Dungeon dungeon) {
        this.currentDungeon = dungeon;
    }

    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
