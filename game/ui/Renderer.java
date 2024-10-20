package game.ui;

import game.Constants;
import game.Game;
import game.object.entity.Actor;
import game.object.entity.Door;
import game.object.entity.Entity;
import game.object.entity.Floor;

import java.awt.*;
import java.util.Objects;

public class Renderer {
    private final Game game;
    private final String characterImage;
    private final ImageCache imageCache;

    public Renderer(Game game, String characterImage, ImageCache imageCache) {
        this.game = game;
        this.characterImage = characterImage;
        this.imageCache = imageCache;
    }

    public void render(Graphics g) {
        renderTopBar(g);
        renderTiles(g);
        if (this.game.getGameState().getMessage().getText() != null) renderMessage(g);
    }

    private void renderTopBar(Graphics g) {
        // Draw top bar background
        g.setColor(new Color(50, 50, 50)); // Dark gray color
        g.fillRect(0, 0, this.game.getWidth(), Constants.Y_OFFSET - 8);

        // Draw level and character name
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times", Font.BOLD, 20));
        g.drawString("Level " + this.game.getGameState().getLevel(), 15, Constants.Y_OFFSET - 16);

        if (Objects.equals(this.characterImage, "/images/geralt.png")) {
            g.drawString("Geralt", 825, Constants.Y_OFFSET - 16);
        } else if (Objects.equals(this.characterImage, "/images/yen.png")) {
            g.drawString("Yennefer", 800, Constants.Y_OFFSET - 16);
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

    private void renderTiles(Graphics g) {
        for (int y = 0; y < Constants.GAME_TILE_NUM; y++) {
            for (int x = 0; x < Constants.GAME_TILE_NUM; x++) {
                Entity tile = this.game.getGameState().getCurrentDungeon().getTile(x, y);
                Image imageToDraw = imageCache.getImage(tile.getImagePath());
                if (imageToDraw != null) {
                    g.drawImage(imageToDraw, x * Constants.GAME_TILE_SIZE, Constants.Y_OFFSET - 8 + y * Constants.GAME_TILE_SIZE, Constants.GAME_TILE_SIZE, Constants.GAME_TILE_SIZE, game);
                }
            }
        }
    }

    public void replacePreviousTile(Actor actor) {
        if (this.game.getGameState().getCurrentDungeon().isDoor(actor.getX(), actor.getY())) {
            this.game.getGameState().getCurrentDungeon().setTile(actor.getX(), actor.getY(), new Door());
        } else {
            this.game.getGameState().getCurrentDungeon().setTile(actor.getX(), actor.getY(), new Floor()); // Otherwise, redraw the floor
        }
    }
}
