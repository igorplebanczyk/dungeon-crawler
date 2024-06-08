package src;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class StartMenu extends JFrame {
    private static final int TILE_SIZE_SELECTION = 200;
    private static final int TILE_SIZE_GAME = 60;
    private static final int GAME_WIDTH = 15;
    private static final int GAME_HEIGHT = 15;
    private static final int SELECTION_WIDTH = 4;
    private static final int SELECTION_HEIGHT = 4;
    private static final int Y_OFFSET_GAME = 70;

    public StartMenu() {
        // Set up the frame
        setTitle("Dungeon Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setSize(SELECTION_WIDTH * TILE_SIZE_SELECTION, SELECTION_HEIGHT * TILE_SIZE_SELECTION);

        // Show the start panel
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
        addFloorTiles(panel);
        return panel;
    }

    // Add the start button to the panel
    private void addStartButton(JPanel panel) {
        JButton startButton = createButton("Start", TILE_SIZE_SELECTION * 2, 48, 4, 4);
        startButton.addActionListener(_ -> switchToSelectionPanel());
        GridBagConstraints gbc = createGridBagConstraints(1, 1, 2, 2, TILE_SIZE_SELECTION / 3, 0);
        panel.add(startButton, gbc);
    }

    // Add the title labels to the panel
    private void addTitleLabels(JPanel panel) {
        JLabel topTitleLabel = createTitleLabel("Dungeon");
        JLabel bottomTitleLabel = createTitleLabel("Crawler");

        GridBagConstraints gbcTopTitle = createGridBagConstraints(1, 0, 2, 1, 0, TILE_SIZE_SELECTION / 5);
        GridBagConstraints gbcBottomTitle = createGridBagConstraints(1, 0, 2, 1, TILE_SIZE_SELECTION / 2, 0);

        panel.add(topTitleLabel, gbcTopTitle);
        panel.add(bottomTitleLabel, gbcBottomTitle);
    }

    // Add the floor tiles to the panel
    private void addFloorTiles(JPanel panel) {
        ImageIcon floorIcon = createScaledIcon("/images/floor.png");

        for (int y = 0; y < SELECTION_HEIGHT; y++) {
            for (int x = 0; x < SELECTION_WIDTH; x++) {
                JLabel label = new JLabel(floorIcon);
                label.setPreferredSize(new Dimension(TILE_SIZE_SELECTION, TILE_SIZE_SELECTION));
                GridBagConstraints gbc = createGridBagConstraints(x, y, 1, 1, 0, 0);
                panel.add(label, gbc);
            }
        }
    }

    // Switch to the selection panel
    private void switchToSelectionPanel() {
        setContentPane(getSelectionPanel());
        revalidate();
    }

    // Create the selection panel
    public JPanel getSelectionPanel() {
        JPanel panel = createBackgroundPanel();
        char[][] layout = {
                { '.', '.', '.', '.' },
                { '.', 'G', 'Y', '.' },
                { '.', 'g', 'y', '.' },
                { '.', '.', '.', '.' }
        };
        populateSelectionPanel(panel, layout);
        return panel;
    }

    // Create the background for the selection panel
    private JPanel createBackgroundPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawFloorTiles(g);
            }
        };
        panel.setLayout(new GridLayout(SELECTION_HEIGHT, SELECTION_WIDTH));
        return panel;
    }

    // Draw the floor tiles
    private void drawFloorTiles(Graphics g) {
        Image floorImage = loadFloorImage();
        for (int y = 0; y < SELECTION_HEIGHT; y++) {
            for (int x = 0; x < SELECTION_WIDTH; x++) {
                g.drawImage(floorImage, x * TILE_SIZE_SELECTION, y * TILE_SIZE_SELECTION, TILE_SIZE_SELECTION, TILE_SIZE_SELECTION, this);
            }
        }
    }

    // Populate the selection panel with floor tiles
    private void populateSelectionPanel(JPanel panel, char[][] layout) {
        for (int y = 0; y < SELECTION_HEIGHT; y++) {
            for (int x = 0; x < SELECTION_WIDTH; x++) {
                JLabel label = createTileLabel(layout[y][x]);
                panel.add(label);
            }
        }
    }

    // Create character image tiles and selection buttons
    private JLabel createTileLabel(char tile) {
        return switch (tile) {
            case 'G' -> createCharacterLabel("/images/geralt.png");
            case 'Y' -> createCharacterLabel("/images/yen.png");
            case 'g' -> createSelectionButton("Select Geralt", "/images/geralt.png", 4, 2);
            case 'y' -> createSelectionButton("Select Yennefer", "/images/yen.png", 2, 4);
            default -> new JLabel();
        };
    }

    // Create a character image tile
    private JLabel createCharacterLabel(String imagePath) {
        ImageIcon icon = createScaledIcon(imagePath);
        return new JLabel(icon);
    }

    // Create a selection button
    private JLabel createSelectionButton(String text, String characterImagePath, int borderLeft, int borderRight) {
        JButton button = createButton(text, TILE_SIZE_SELECTION, 16, borderLeft, borderRight);
        button.addActionListener(_ -> launchGame(characterImagePath));
        JLabel label = new JLabel();
        label.setLayout(new BorderLayout());
        label.add(button);
        return label;
    }

    // Create a button
    private JButton createButton(String text, int width, int fontSize, int borderLeft, int borderRight) {
        JButton button = new JButton(text);
        Dimension buttonSize = new Dimension(width, TILE_SIZE_SELECTION);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 70, 70));
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setFocusable(false);
        button.setBorder(BorderFactory.createMatteBorder(4, borderLeft, 4, borderRight, Color.BLACK));
        return button;
    }

    // Create a title
    private JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Dialog", Font.BOLD, 72));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }

    // Create a grid for the layout in start panel
    private GridBagConstraints createGridBagConstraints(int gridX, int gridY, int gridWidth, int gridHeight, int top, int bottom) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridX;
        gbc.gridy = gridY;
        gbc.gridwidth = gridWidth;
        gbc.gridheight = gridHeight;
        gbc.insets = new Insets(top, 0, bottom, 0);
        return gbc;
    }

    // Create a scaled icon
    private ImageIcon createScaledIcon(String path) {
        return new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(path))).getImage().getScaledInstance(StartMenu.TILE_SIZE_SELECTION, StartMenu.TILE_SIZE_SELECTION, Image.SCALE_SMOOTH));
    }

    // Load floor image
    private Image loadFloorImage() {
        try {
            return new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/floor.png"))).getImage();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Launch the actual game
    private void launchGame(String characterImagePath) {
        dispose();
        new Game(characterImagePath, TILE_SIZE_GAME, GAME_WIDTH, GAME_HEIGHT, Y_OFFSET_GAME);
    }
}
