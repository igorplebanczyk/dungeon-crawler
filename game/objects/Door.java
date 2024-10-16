package game.objects;

import game.Constants;

public class Door extends GameObject {
    public Door() {
        this.type = GameObjectType.DOOR;
        this.imagePath = Constants.OBJECT_IMAGE_MAP.get(this.type);
    }
}
