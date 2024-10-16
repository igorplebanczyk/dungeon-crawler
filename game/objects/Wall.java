package game.objects;

import game.Constants;

public class Wall extends GameObject {
    public Wall() {
        this.type = GameObjectType.WALL;
        this.imagePath = Constants.OBJECT_IMAGE_MAP.get(this.type);
    }
}
