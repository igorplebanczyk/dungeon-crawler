package game.objects;

public abstract class GameObject {
    protected String imagePath;
    protected Tile type;

    public String getImagePath() {
        return imagePath;
    }

    public Tile getType() {
        return type;
    }

    public void setType(Tile type) {
        this.type = type;
    }
}
