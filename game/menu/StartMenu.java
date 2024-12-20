package game.menu;

import game.Constants;
import game.Game;
import game.ImageCache;
import game.object.entity.EntityType;
import game.object.entity.PlayerCharacter;
import game.object.entity.StaticEntity;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class StartMenu extends Menu {
    public StartMenu() {
        setTitle("Dungeon Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(Constants.START_WINDOW_WIDTH * Constants.START_TILE_SIZE, Constants.START_WINDOW_HEIGHT * Constants.START_TILE_SIZE);

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
        addFloorTiles(panel, Constants.START_TILE_SIZE, Constants.START_WINDOW_WIDTH, Constants.START_WINDOW_HEIGHT);
        return panel;
    }

    // Add the start button to the panel
    private void addStartButton(JPanel panel) {
        JButton startButton = getButton("Start", Constants.START_TILE_SIZE * 2, 48, 4, 4);
        startButton.addActionListener(_ -> switchToSelectionPanel());
        GridBagConstraints gbc = createGridBagConstraints(1, 1, 2, 2, Constants.START_TILE_SIZE / 3, 0);
        panel.add(startButton, gbc);
    }

    // Add the title labels to the panel
    private void addTitleLabels(JPanel panel) {
        JLabel topTitleLabel = getTitleLabel("Dungeon");
        JLabel bottomTitleLabel = getTitleLabel("Crawler");

        GridBagConstraints gbcTopTitle = createGridBagConstraints(1, 0, 2, 1, 0, Constants.START_TILE_SIZE / 5);
        GridBagConstraints gbcBottomTitle = createGridBagConstraints(1, 0, 2, 1, Constants.START_TILE_SIZE / 2, 0);

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
        Tile[][] layout = {
                {Tile.BACKGROUND, Tile.BACKGROUND, Tile.BACKGROUND, Tile.BACKGROUND},
                {Tile.BACKGROUND, Tile.CHARACTER_1_IMG, Tile.CHARACTER_2_IMG, Tile.BACKGROUND},
                {Tile.BACKGROUND, Tile.CHARACTER_1_TEXT, Tile.CHARACTER_2_TEXT, Tile.BACKGROUND},
                {Tile.BACKGROUND, Tile.BACKGROUND, Tile.BACKGROUND, Tile.BACKGROUND}
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
        panel.setLayout(new GridLayout(Constants.START_WINDOW_HEIGHT, Constants.START_WINDOW_WIDTH));
        return panel;
    }

    // Draw the floor tiles
    private void drawSelectionPanelFloorTiles(Graphics g) {
        for (int y = 0; y < Constants.START_WINDOW_HEIGHT; y++) {
            for (int x = 0; x < Constants.START_WINDOW_WIDTH; x++) {
                g.drawImage(ImageCache.getImage(new StaticEntity(EntityType.FLOOR).imagePath()), x * Constants.START_TILE_SIZE, y * Constants.START_TILE_SIZE, Constants.START_TILE_SIZE, Constants.START_TILE_SIZE, this);
            }
        }
    }

    // Populate the selection panel with floor tiles
    private void fillSelectionPanel(JPanel panel, Tile[][] layout) {
        for (int y = 0; y < Constants.START_WINDOW_HEIGHT; y++) {
            for (int x = 0; x < Constants.START_WINDOW_WIDTH; x++) {
                JLabel label = createSelectionTiles(layout[y][x]);
                panel.add(label);
            }
        }
    }

    // Create character image tiles and selection buttons
    private JLabel createSelectionTiles(Tile tile) {
        List<PlayerCharacter> characters = Arrays.asList(PlayerCharacter.values());

        return switch (tile) {
            case Tile.CHARACTER_1_IMG -> getCharacterLabel(Constants.PLAYER_IMAGE_MAP.get(characters.get(0)));
            case Tile.CHARACTER_2_IMG -> getCharacterLabel(Constants.PLAYER_IMAGE_MAP.get(characters.get(1)));
            case Tile.CHARACTER_1_TEXT -> getSelectionButton(characters.get(0).getName(), characters.get(0), 4, 2);
            case Tile.CHARACTER_2_TEXT -> getSelectionButton(characters.get(1).getName(), characters.get(1), 2, 4);
            default -> new JLabel();
        };
    }

    // Create a character image tile
    private JLabel getCharacterLabel(String imagePath) {
        ImageIcon icon = createScaledIcon(imagePath, Constants.START_TILE_SIZE);
        return new JLabel(icon);
    }

    // Create a selection button
    private JLabel getSelectionButton(String text, PlayerCharacter character, int borderLeft, int borderRight) {
        JButton button = getButton(text, Constants.START_TILE_SIZE, 16, borderLeft, borderRight);
        button.addActionListener(_ -> launchGame(character));
        JLabel label = new JLabel();
        label.setLayout(new BorderLayout());
        label.add(button);
        return label;
    }

    // Create a button
    private JButton getButton(String text, int width, int fontSize, int borderLeft, int borderRight) {
        JButton button = new JButton(text);
        configureButton(button, width, Constants.START_TILE_SIZE, fontSize, borderLeft, borderRight);
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
    private void launchGame(PlayerCharacter character) {
        dispose();
        new Game(character);
    }
}
