package game.objects;

public class Wall extends GameObject {
    public Wall() {
        this.imagePath = "/images/wall.png";
        this.type = GameObjectType.WALL;
    }
}
