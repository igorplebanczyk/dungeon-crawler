package game;

import game.objects.GameObject;

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

    public void render(Graphics g, int level, Dungeon currentDungeon, String message) {
        drawTopBar(g, level);
        drawTiles(g, currentDungeon);
        if (message != null) drawMessage(g, message);
    }

    private void drawTopBar(Graphics g, int level) {
        // Draw top bar background
        g.setColor(new Color(50, 50, 50)); // Dark gray color
        g.fillRect(0, 0, game.getWidth(), Constants.Y_OFFSET - 8);

        // Draw level and character name
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times", Font.BOLD, 20));
        g.drawString("Level " + level, 15, Constants.Y_OFFSET - 16);
        if (Objects.equals(characterImage, "/images/geralt.png")) {
            g.drawString("Geralt", 825, Constants.Y_OFFSET - 16);
        } else if (Objects.equals(characterImage, "/images/yen.png")) {
            g.drawString("Yennefer", 800, Constants.Y_OFFSET - 16);
        }
    }

    private void drawMessage(Graphics g, String message) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times ", Font.BOLD, 56));
        FontMetrics fm = g.getFontMetrics();
        int messageWidth = fm.stringWidth(message);
        int messageHeight = fm.getHeight();
        int x = (game.getWidth() - messageWidth) / 2;
        int y = (game.getHeight() - messageHeight) / 2 + fm.getAscent();
        g.drawString(message, x, y);
    }

    private void drawTiles(Graphics g, Dungeon currentDungeon) {
        for (int y = 0; y < Constants.GAME_TILE_NUM; y++) {
            for (int x = 0; x < Constants.GAME_TILE_NUM; x++) {
                GameObject tile = currentDungeon.getTile(x, y);
                Image imageToDraw = imageCache.getImage(tile.getImagePath());
                if (imageToDraw != null) {
                    g.drawImage(imageToDraw, x * Constants.GAME_TILE_SIZE, Constants.Y_OFFSET - 8 + y * Constants.GAME_TILE_SIZE, Constants.GAME_TILE_SIZE, Constants.GAME_TILE_SIZE, game);
                }
            }
        }
    }
}
