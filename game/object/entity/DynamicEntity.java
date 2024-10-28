package game.object.entity;

import game.Constants;
import game.Game;

public abstract class DynamicEntity implements Entity {
    protected int x;
    protected int y;
    protected EntityType type;
    protected String imagePath;
    protected Game game;

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public EntityType getType() {
        return type;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void move(int dx, int dy) {
        this.game.getRenderer().replacePreviousTile(this); // Clear the previous player position and redraw either a door or floor

        this.x += dx;
        this.y += dy;

        if (this.game.getGameState().getCurrentDungeon().isDoor(this.x, this.y)) {
            moveToAdjacentRoom(this.x, this.y);
        }

        if (this.x == this.game.getGameState().getCurrentDungeon().getExitX()
                && this.y == this.game.getGameState().getCurrentDungeon().getExitY()) {
            game.advanceToNextLevel();
        }

        this.game.getGameState().getCurrentDungeon().setTile(this.x, this.y, this); // Draw the player at the new position

        this.game.repaint();
    }

    private void moveToAdjacentRoom(int newX, int newY) {
        if (newX == 0) {
            this.game.getGameState().setCurrentDungeon(this.game.getMap().getGrid()[this.game.getGameState().getCurrentDungeon().getGridX() - 1][this.game.getGameState().getCurrentDungeon().getGridY()]); // Left edge
            this.x = Constants.GAME_TILE_NUM - 1;
        } else if (newX == Constants.GAME_TILE_NUM - 1) {
            this.game.getGameState().setCurrentDungeon(this.game.getMap().getGrid()[this.game.getGameState().getCurrentDungeon().getGridX() + 1][this.game.getGameState().getCurrentDungeon().getGridY()]); // Right edge
            this.x = 0;
        } else if (newY == 0) {
            this.game.getGameState().setCurrentDungeon(this.game.getMap().getGrid()[this.game.getGameState().getCurrentDungeon().getGridX()][this.game.getGameState().getCurrentDungeon().getGridY() - 1]); // Top edge
            this.y = Constants.GAME_TILE_NUM - 1;
        } else if (newY == Constants.GAME_TILE_NUM - 1) {
            this.game.getGameState().setCurrentDungeon(this.game.getMap().getGrid()[this.game.getGameState().getCurrentDungeon().getGridX()][this.game.getGameState().getCurrentDungeon().getGridY() + 1]); // Bottom edge
            this.y = 0;
        }
    }
}
