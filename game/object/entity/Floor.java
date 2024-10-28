package game.object.entity;

import game.Constants;

public record Floor() implements Entity {
    private static final EntityType TYPE = EntityType.FLOOR;
    private static final String IMAGE_PATH = Constants.OBJECT_IMAGE_MAP.get(TYPE);

    public EntityType getType() {
        return TYPE;
    }

    public String getImagePath() {
        return IMAGE_PATH;
    }
}
