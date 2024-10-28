package game.object.entity;

import game.Constants;

public record StaticEntity(EntityType type) implements Entity {
    public String imagePath() {
        return Constants.OBJECT_IMAGE_MAP.get(type);
    }
}
