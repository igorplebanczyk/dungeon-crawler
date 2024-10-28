package game.object.entity;

import game.Game;
import game.object.Dungeon;

public class Player extends DynamicEntity {
    public Player(Dungeon dungeon, int x, int y, String imagePath, Game game) {
        this.x = x;
        this.y = y;
        this.imagePath = imagePath;
        this.type = EntityType.PLAYER;
        this.game = game;
        dungeon.setTile(x, y, this);
    }
}
