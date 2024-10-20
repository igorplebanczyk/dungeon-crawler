package game;

public class GameState {
    private int level;
    private boolean paused = false;
    private boolean movementInProgress = false;
    private static boolean bulldozerMode = false;
    private Dungeon currentDungeon;

    public GameState() {
        this.level = 1;
    }

    public int getLevel() {
        return this.level;
    }

    public void incLevel() {
        this.level++;
    }

    public Dungeon getCurrentDungeon() {
        return this.currentDungeon;
    }

    public void setCurrentDungeon(Dungeon dungeon) {
        this.currentDungeon = dungeon;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void togglePause() {
        this.paused = !this.paused;
    }

    public static boolean isBulldozerMode() {
        return bulldozerMode;
    }

    public static void setBulldozerMode(boolean mode) {
        bulldozerMode = mode;
    }

    public boolean isMovementInProgress() {
        return movementInProgress;
    }

    public void setMovementInProgress(boolean movementInProgress) {
        this.movementInProgress = movementInProgress;
    }
}
