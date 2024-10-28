package game.object.entity;

import game.Constants;

public record StaticEntity(EntityType type) implements Entity {
    public EntityType getType() {
        return type;
    }

    public String getImagePath() {
        return Constants.OBJECT_IMAGE_MAP.get(type);
    }
}
