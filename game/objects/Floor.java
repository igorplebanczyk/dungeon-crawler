package game.objects;

import game.Constants;

public class Floor extends GameObject {
    public Floor() {
        this.type = GameObjectType.FLOOR;
        this.imagePath = Constants.OBJECT_IMAGE_MAP.get(this.type);
    }
}
