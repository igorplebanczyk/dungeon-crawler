package game;

import game.object.entity.*;

import java.awt.*;

public class Renderer {
    private final Game game;

    public Renderer(Game game) {
        this.game = game;
    }

    public void render(Graphics g) {
        renderTiles(g);
        if (this.game.getGameState().getMessage().getText() != null) renderMessage(g);
    }

    private void renderTiles(Graphics g) {
        for (int y = 0; y < Constants.GAME_TILE_NUM; y++) {
            for (int x = 0; x < Constants.GAME_TILE_NUM; x++) {
                Entity tile = this.game.getGameState().getCurrentDungeon().getTile(x, y);
                Image imageToDraw = ImageCache.getImage(tile.imagePath());
                if (imageToDraw != null) {
                    g.drawImage(imageToDraw, x * Constants.GAME_TILE_SIZE, y * Constants.GAME_TILE_SIZE, Constants.GAME_TILE_SIZE, Constants.GAME_TILE_SIZE, game);
                }
            }
        }
    }

    private void renderMessage(Graphics g) {
        g.setColor(Constants.MESSAGE_COLOR);
        g.setFont(Constants.MESSAGE_FONT);
        FontMetrics fm = g.getFontMetrics();
        int messageWidth = fm.stringWidth(this.game.getGameState().getMessage().getText());
        int messageHeight = fm.getHeight();
        int x = (this.game.getWidth() - messageWidth) / 2;
        int y = (this.game.getHeight() - messageHeight) / 2 + fm.getAscent();
        g.drawString(this.game.getGameState().getMessage().getText(), x, y);
    }

    public void replacePreviousTile(DynamicEntity actor) {
        if (this.game.getGameState().getCurrentDungeon().isDoor(actor.getX(), actor.getY())) {
            this.game.getGameState().getCurrentDungeon().setTile(actor.getX(), actor.getY(), new StaticEntity(EntityType.DOOR)); // If the tile is a door, redraw the door
        } else {
            this.game.getGameState().getCurrentDungeon().setTile(actor.getX(), actor.getY(), new StaticEntity(EntityType.FLOOR)); // Otherwise, redraw the floor
        }
    }
}
