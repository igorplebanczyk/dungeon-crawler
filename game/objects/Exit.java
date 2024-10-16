package game.objects;

import game.Constants;

public class Exit extends GameObject {
    public Exit() {
        this.type = GameObjectType.EXIT;
        this.imagePath = Constants.OBJECT_IMAGE_MAP.get(this.type);
    }
}
