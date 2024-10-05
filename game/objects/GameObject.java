package game.objects;

public abstract class GameObject {
    protected String imagePath;
    protected GameObjectType type;

    public String getImagePath() {
        return imagePath;
    }

    public GameObjectType getType() {
        return type;
    }
}
