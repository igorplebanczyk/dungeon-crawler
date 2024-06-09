package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class PauseMenu extends JFrame {
    private static final int PAUSE_MENU_TILE_SIZE = 200;
    private static final int PAUSE_MENU_WINDOW_WIDTH = 2;
    private static final int PAUSE_MENU_WINDOW_HEIGHT = 2;

    private final Game game;

    public PauseMenu(JFrame parent) {
        // Set up the frame
        setTitle("Pause Menu");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(PAUSE_MENU_WINDOW_WIDTH * PAUSE_MENU_TILE_SIZE, PAUSE_MENU_WINDOW_HEIGHT * PAUSE_MENU_TILE_SIZE);

        this.game = (Game) parent;

        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    PauseMenu.this.setVisible(false);
                    ((Game)parent).pause();
                }
            }
        });
        this.setFocusable(true);
        this.requestFocusInWindow();

        // Create the main panel with a GridLayout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        addButton(getResumeButton(), mainPanel, 0);
        addButton(getRefreshButton(), mainPanel, 150);
        addButton(getQuitButton(), mainPanel, 300);
        addFloorTiles(mainPanel);

        add(mainPanel);

        // Center the frame on the screen
        setLocationRelativeTo(parent);
    }

    // Add the floor tiles to the panel
    private void addFloorTiles(JPanel panel) {
        ImageIcon floorIcon = createScaledIcon("/images/floor.png");

        for (int y = 0; y < PAUSE_MENU_WINDOW_HEIGHT; y++) {
            for (int x = 0; x < PAUSE_MENU_WINDOW_WIDTH; x++) {
                JLabel label = new JLabel(floorIcon);
                label.setPreferredSize(new Dimension(PAUSE_MENU_TILE_SIZE, PAUSE_MENU_TILE_SIZE));
                GridBagConstraints gbc = createGridBagConstraints(x, y, 1, 1, 0, 0);
                panel.add(label, gbc);
            }
        }
    }

    private void addButton(JButton button, JPanel panel, int offsetTop) {
        GridBagConstraints gbc = createGridBagConstraints(1, 0, 2, 2, offsetTop, 0);
        button.setPreferredSize(new Dimension(200, 75));
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
            StartMenu startMenu = new StartMenu(); // Create new start menu
            JPanel characterPanel = startMenu.getSelectionPanel(); // Get the character selection panel

            game.dispose(); // Close the current game
            startMenu.setContentPane(characterPanel); // Set the character selection panel as the content pane
            characterPanel.setVisible(true);
        });
        return refreshButton;
    }

    private JButton getQuitButton() {
        JButton quitButton = new JButton("Exit");
        quitButton.addActionListener(_ -> System.exit(0));
        return quitButton;
    }

    // Create a scaled icon
    private ImageIcon createScaledIcon(String path) {
        return new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(path))).getImage().getScaledInstance(PAUSE_MENU_TILE_SIZE, PAUSE_MENU_TILE_SIZE, Image.SCALE_SMOOTH));
    }

    // Create a grid for the layout in start panel
    private GridBagConstraints createGridBagConstraints(int gridX, int gridY, int gridWidth, int gridHeight, int top, int bottom) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridX;
        gbc.gridy = gridY;
        gbc.gridwidth = gridWidth;
        gbc.gridheight = gridHeight;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(top, 0, bottom, 0);
        return gbc;
    }
}