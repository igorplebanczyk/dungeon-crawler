package game.object.entity;

import game.object.Dungeon;

public class Player extends Actor {
    private Dungeon dungeon;

    public Player(Dungeon dungeon, int x, int y, String imagePath) {
        this.x = x;
        this.y = y;
        this.imagePath = imagePath;
        this.type = EntityType.PLAYER;
        this.dungeon = dungeon;
        this.dungeon.setTile(x, y, this);
    }
}
