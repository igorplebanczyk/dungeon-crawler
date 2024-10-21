package game.object.entity;

public enum PlayerCharacter {
    GERALT("Geralt"),
    YENNEFER("Yennefer"),
    ;

    private final String name;

    PlayerCharacter(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
