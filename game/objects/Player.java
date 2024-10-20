package game.objects;

import game.Dungeon;

public class Player extends Entity {
    public Player(Dungeon dungeon, int x, int y, String imagePath) {
        this.x = x;
        this.y = y;
        this.imagePath = imagePath;
        this.type = GameObjectType.PLAYER;

        dungeon.setTile(x, y, this);
    }
}
