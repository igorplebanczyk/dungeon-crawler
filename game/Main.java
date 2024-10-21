package game;

import game.menu.StartMenu;

public class Main {
    public static void main(String[] args) {
        new ImageCache().cacheImages();
        new StartMenu();
    }
}
