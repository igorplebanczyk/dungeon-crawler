package game.object.entity;

import game.Constants;

public record Wall() implements Entity {
    private static final EntityType TYPE = EntityType.WALL;
    private static final String IMAGE_PATH = Constants.OBJECT_IMAGE_MAP.get(TYPE);

    public EntityType getType() {
        return TYPE;
    }

    public String getImagePath() {
        return IMAGE_PATH;
    }
}
