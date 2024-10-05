package game.objects;

public class Wall extends GameObject {
    public Wall(int x, int y) {
        this.x = x;
        this.y = y;
        this.imagePath = "/images/wall.png";
    }
}
