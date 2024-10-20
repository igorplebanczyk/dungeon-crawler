package game.object.entity;

public abstract class Entity {
    protected String imagePath;
    protected EntityType type;

    public String getImagePath() {
        return imagePath;
    }

    public EntityType getType() {
        return type;
    }
}
