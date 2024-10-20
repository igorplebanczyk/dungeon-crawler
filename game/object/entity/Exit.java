package game.object.entity;

import game.Constants;

public class Exit extends Entity {
    public Exit() {
        this.type = EntityType.EXIT;
        this.imagePath = Constants.OBJECT_IMAGE_MAP.get(this.type);
    }
}
