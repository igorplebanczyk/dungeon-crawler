package game.ui;

import game.Game;

import javax.swing.*;

public class Message {
    private String text;
    private Timer timer;
    private final Game game;

    public Message(String text, Game game) {
        this.text = text;
        this.timer = null;
        this.game = game;
    }

    public String getText() {
        return this.text;
    }

    public void display(int duration) {
        if (timer != null) {
            timer.stop();
        }

        this.timer = new Timer(duration, _ -> {
            this.text = null; // Clear the message after the duration
            this.game.repaint();
        });
        timer.setRepeats(false);
        timer.start();

        this.game.repaint();
    }
}
