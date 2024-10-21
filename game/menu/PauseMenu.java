package game.menu;

import game.Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import game.Constants;

public class PauseMenu extends Menu {
    private final Game game;

    public PauseMenu(JFrame parent) {
        setTitle("Pause Menu");
        this.game = (Game) parent;

        setUndecorated(true);
        setSize(Constants.PAUSE_WINDOW_WIDTH * Constants.PAUSE_TILE_SIZE,
                Constants.PAUSE_WINDOW_HEIGHT * Constants.PAUSE_TILE_SIZE);
        getRootPane().setBorder(BorderFactory.createLineBorder(Color.BLACK, 6));

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    PauseMenu.this.setVisible(false);
                    ((Game) parent).pause();
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();

        add(getPausePanel());
        setLocationRelativeTo(parent);
    }

    // Create the pause panel
    private JPanel getPausePanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        addButton(getResumeButton(), mainPanel, 0, 200);
        addButton(getRestartButton(), mainPanel, 0, 0);
        addButton(getQuitButton(), mainPanel, 200, 0);
        addFloorTiles(mainPanel, Constants.PAUSE_TILE_SIZE, Constants.PAUSE_WINDOW_WIDTH, Constants.PAUSE_WINDOW_HEIGHT);
        return mainPanel;
    }

    // Add a button to the panel
    private void addButton(JButton button, JPanel panel, int offsetTop, int offsetBottom) {
        GridBagConstraints gbc = createGridBagConstraints(0, 0,
                Constants.PAUSE_WINDOW_WIDTH, Constants.PAUSE_WINDOW_HEIGHT, offsetTop, offsetBottom);

        configureButton(button, Constants.PAUSE_TILE_SIZE, Constants.PAUSE_TILE_SIZE / 2,
                24, 4, 4);
        panel.add(button, gbc);
    }

    private JButton getResumeButton() {
        JButton resumeButton = new JButton("Resume");
        resumeButton.addActionListener(_ -> {
            game.pause();
            PauseMenu.this.setVisible(false);
        });
        return resumeButton;
    }

    private JButton getRestartButton() {
        JButton restartButton = new JButton("Restart");
        restartButton.addActionListener(_ -> {
            StartMenu startMenu = new StartMenu();
            this.dispose();
            game.dispose();
            startMenu.setVisible(true);
        });
        return restartButton;
    }

    private JButton getQuitButton() {
        JButton quitButton = new JButton("Exit");
        quitButton.addActionListener(_ -> System.exit(0));
        return quitButton;
    }
}
