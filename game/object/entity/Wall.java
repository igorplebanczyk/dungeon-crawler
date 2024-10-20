package game.object.entity;

import game.Constants;

public class Wall extends Entity {
    public Wall() {
        this.type = EntityType.WALL;
        this.imagePath = Constants.OBJECT_IMAGE_MAP.get(this.type);
    }
}
