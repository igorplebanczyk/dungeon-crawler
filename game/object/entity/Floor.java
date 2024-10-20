package game.object.entity;

import game.Constants;

public class Floor extends Entity {
    public Floor() {
        this.type = EntityType.FLOOR;
        this.imagePath = Constants.OBJECT_IMAGE_MAP.get(this.type);
    }
}
