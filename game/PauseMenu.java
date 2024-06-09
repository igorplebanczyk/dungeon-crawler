package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PauseMenu extends Menu {
    private final Game game;

    private static final int TILE_SIZE = 150;
    private static final int WINDOW_WIDTH = 3;
    private static final int WINDOW_HEIGHT = 3;

    public PauseMenu(JFrame parent) {
        setTitle("Pause Menu");
        this.game = (Game) parent;

        setUndecorated(true);
        setSize(WINDOW_WIDTH * TILE_SIZE, WINDOW_HEIGHT * TILE_SIZE);
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
        addButton(getRefreshButton(), mainPanel, 0, 0);
        addButton(getQuitButton(), mainPanel, 200, 0);
        addFloorTiles(mainPanel, TILE_SIZE, WINDOW_WIDTH, WINDOW_HEIGHT);
        return mainPanel;
    }

    // Add a button to the panel
    private void addButton(JButton button, JPanel panel, int offsetTop, int offsetBottom) {
        GridBagConstraints gbc = createGridBagConstraints(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, offsetTop, offsetBottom);
        configureButton(button, TILE_SIZE, TILE_SIZE / 2, 24, 4, 4);
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
