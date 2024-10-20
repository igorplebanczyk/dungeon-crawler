package game.object.entity;

import game.Constants;

public class Door extends Entity {
    public Door() {
        this.type = EntityType.DOOR;
        this.imagePath = Constants.OBJECT_IMAGE_MAP.get(this.type);
    }
}
