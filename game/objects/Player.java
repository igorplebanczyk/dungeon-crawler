package game.objects;

public class Player extends Entity {
    public Player(int x, int y, String imagePath) {
        this.x = x;
        this.y = y;
        this.imagePath = imagePath;
        this.type = GameObjectType.PLAYER;
    }
}
