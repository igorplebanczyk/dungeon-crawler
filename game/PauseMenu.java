package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PauseMenu extends Menu {
    private final Game game;

    public PauseMenu(JFrame parent) {
        setTitle("Pause Menu");
        this.game = (Game) parent;

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    PauseMenu.this.setVisible(false);
                    ((Game) parent).pause();
                }
            }
        });
        this.setFocusable(true);
        this.requestFocusInWindow();

        getPausePanel();
        setLocationRelativeTo(parent);
    }

    private void getPausePanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        addButton(getResumeButton(), mainPanel, 0, 200, TILE_SIZE, WINDOW_WIDTH, WINDOW_HEIGHT);
        addButton(getRefreshButton(), mainPanel, 0, 0, TILE_SIZE, WINDOW_WIDTH, WINDOW_HEIGHT);
        addButton(getQuitButton(), mainPanel, 200, 0, TILE_SIZE, WINDOW_WIDTH, WINDOW_HEIGHT);
        addFloorTiles(mainPanel, TILE_SIZE, WINDOW_WIDTH, WINDOW_HEIGHT);
        add(mainPanel);
    }

    private JButton getResumeButton() {
        JButton resumeButton = new JButton("Resume");
        resumeButton.addActionListener(_ -> {
            game.pause();
            PauseMenu.this.setVisible(false);
        });
        return resumeButton;
    }

    private JButton getRefreshButton() {
        JButton refreshButton = new JButton("Restart");
        refreshButton.addActionListener(_ -> {
            StartMenu startMenu = new StartMenu();
            JPanel characterPanel = startMenu.getSelectionPanel();

            game.dispose();
            startMenu.setContentPane(characterPanel);
            characterPanel.setVisible(true);
        });
        return refreshButton;
    }

    private JButton getQuitButton() {
        JButton quitButton = new JButton("Exit");
        quitButton.addActionListener(_ -> System.exit(0));
        return quitButton;
    }
}
