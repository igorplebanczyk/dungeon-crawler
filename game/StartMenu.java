package game;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StartMenu extends Menu {
    private static final int TILE_SIZE = 200;
    private static final int WINDOW_WIDTH = 4;
    private static final int WINDOW_HEIGHT = 4;
    private static final int GAME_TILE_SIZE = 60;
    private static final int GAME_WIDTH = 15;
    private static final int GAME_HEIGHT = 15;
    private static final int GAME_Y_OFFSET = 70;

    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

    public StartMenu() {
        setTitle("Dungeon Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH * TILE_SIZE, WINDOW_HEIGHT * TILE_SIZE);

        add(getStartPanel());
        setVisible(true);
        centerFrameOnScreen();
    }

    // Center the frame on the screen
    private void centerFrameOnScreen() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
    }

    // Create the start panel
    private JPanel getStartPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        addStartButton(panel);
        addTitleLabels(panel);
        addFloorTiles(panel, TILE_SIZE, WINDOW_WIDTH, WINDOW_HEIGHT);
        return panel;
    }

    // Add the start button to the panel
    private void addStartButton(JPanel panel) {
        JButton startButton = getButton("Start", TILE_SIZE * 2, 48, 4, 4);
        startButton.addActionListener(_ -> switchToSelectionPanel());
        GridBagConstraints gbc = createGridBagConstraints(1, 1, 2, 2, TILE_SIZE / 3, 0);
        panel.add(startButton, gbc);
    }

    // Add the title labels to the panel
    private void addTitleLabels(JPanel panel) {
        JLabel topTitleLabel = getTitleLabel("Dungeon");
        JLabel bottomTitleLabel = getTitleLabel("Crawler");

        GridBagConstraints gbcTopTitle = createGridBagConstraints(1, 0, 2, 1, 0, TILE_SIZE / 5);
        GridBagConstraints gbcBottomTitle = createGridBagConstraints(1, 0, 2, 1, TILE_SIZE / 2, 0);

        panel.add(topTitleLabel, gbcTopTitle);
        panel.add(bottomTitleLabel, gbcBottomTitle);
    }

    // Switch to the selection panel
    private void switchToSelectionPanel() {
        setContentPane(getSelectionPanel());
        revalidate();
    }

    // Create the selection panel
    public JPanel getSelectionPanel() {
        JPanel panel = getBackgroundPanel();
        char[][] layout = {
                { '.', '.', '.', '.' },
                { '.', 'G', 'Y', '.' },
                { '.', 'g', 'y', '.' },
                { '.', '.', '.', '.' }
        };
        fillSelectionPanel(panel, layout);
        return panel;
    }

    // Create the background for the selection panel
    private JPanel getBackgroundPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSelectionPanelFloorTiles(g);
            }
        };
        panel.setLayout(new GridLayout(WINDOW_HEIGHT, WINDOW_WIDTH));
        return panel;
    }

    // Draw the floor tiles
    private void drawSelectionPanelFloorTiles(Graphics g) {
        Image floorImage = loadFloorImage();
        for (int y = 0; y < WINDOW_HEIGHT; y++) {
            for (int x = 0; x < WINDOW_WIDTH; x++) {
                g.drawImage(floorImage, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
            }
        }
    }

    // Populate the selection panel with floor tiles
    private void fillSelectionPanel(JPanel panel, char[][] layout) {
        for (int y = 0; y < WINDOW_HEIGHT; y++) {
            for (int x = 0; x < WINDOW_WIDTH; x++) {
                JLabel label = createSelectionTiles(layout[y][x]);
                panel.add(label);
            }
        }
    }

    // Load floor image
    private Image loadFloorImage() {
        try {
            return new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/floor.png"))).getImage();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "An exception occurred", e);
            return null;
        }
    }

    // Create character image tiles and selection buttons
    private JLabel createSelectionTiles(char tile) {
        return switch (tile) {
            case 'G' -> getCharacterLabel("/images/geralt.png");
            case 'Y' -> getCharacterLabel("/images/yen.png");
            case 'g' -> getSelectionButton("Select Geralt", "/images/geralt.png", 4, 2);
            case 'y' -> getSelectionButton("Select Yennefer", "/images/yen.png", 2, 4);
            default -> new JLabel();
        };
    }

    // Create a character image tile
    private JLabel getCharacterLabel(String imagePath) {
        ImageIcon icon = createScaledIcon(imagePath, TILE_SIZE);
        return new JLabel(icon);
    }

    // Create a selection button
    private JLabel getSelectionButton(String text, String characterImagePath, int borderLeft, int borderRight) {
        JButton button = getButton(text, TILE_SIZE, 16, borderLeft, borderRight);
        button.addActionListener(_ -> launchGame(characterImagePath));
        JLabel label = new JLabel();
        label.setLayout(new BorderLayout());
        label.add(button);
        return label;
    }

    // Create a button
    private JButton getButton(String text, int width, int fontSize, int borderLeft, int borderRight) {
        JButton button = new JButton(text);
        configureButton(button, width, TILE_SIZE, fontSize, borderLeft, borderRight);
        return button;
    }

    // Create a title
    private JLabel getTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Dialog", Font.BOLD, 72));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }

    // Launch the actual game
    private void launchGame(String characterImagePath) {
        dispose();
        new Game(characterImagePath, GAME_TILE_SIZE, GAME_WIDTH, GAME_HEIGHT, GAME_Y_OFFSET);
    }
}
