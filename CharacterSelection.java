import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class CharacterSelection extends JFrame {
    private static final int TILE_SIZE_SELECTION = 200; // Size of each tile in the selection grid
    private static final int TILE_SIZE_GAME = 60; // Size of each tile in the game grid
    private static final int GAME_WIDTH = 15; // Width of the game grid
    private static final int GAME_HEIGHT = 15; // Height of the game grid
    private static final int SELECTION_WIDTH = 4; // Width of the selection grid
    private static final int SELECTION_HEIGHT = 4; // Height of the selection grid
    private static final int Y_OFFSET_GAME = 70; // Y offset for drawing the game grid

    public CharacterSelection() {
        setTitle("Character Selection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Set the dimensions of the selection panel to match the smaller grid
        int selectionWidth = SELECTION_WIDTH * TILE_SIZE_SELECTION;
        int selectionHeight = SELECTION_HEIGHT * TILE_SIZE_SELECTION;
        setSize(selectionWidth, selectionHeight);

        JPanel panel = getPanel();

        add(panel);
        setVisible(true);

        // Center the frame on the screen after setting its size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
    }

    private JPanel getPanel() {
        // Create the main panel for the character selection
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the background with 'floor.png' images
                for (int y = 0; y < SELECTION_HEIGHT; y++) {
                    for (int x = 0; x < SELECTION_WIDTH; x++) {
                        Image floorImage = loadImage();
                        g.drawImage(floorImage, x * TILE_SIZE_SELECTION, y * TILE_SIZE_SELECTION, TILE_SIZE_SELECTION, TILE_SIZE_SELECTION, this);
                    }
                }
            }
        };
        panel.setLayout(new GridLayout(SELECTION_HEIGHT, SELECTION_WIDTH));

        // Layout representing the character selection grid
        char[][] layout = {
                { '.', '.', '.', '.' },
                { '.', 'G', 'Y', '.' },
                { '.', 'g', 'y', '.' },
                { '.', '.', '.', '.' }
        };

        // Populate the panel with buttons and labels based on the layout
        for (int y = 0; y < SELECTION_HEIGHT; y++) {
            for (int x = 0; x < SELECTION_WIDTH; x++) {
                char tile = layout[y][x];
                JLabel label;
                switch (tile) {
                    case '.':
                        // Floor tile
                        label = new JLabel();
                        label.setPreferredSize(new Dimension(TILE_SIZE_SELECTION, TILE_SIZE_SELECTION));
                        break;
                    case 'G':
                        // Geralt's character icon
                        label = new JLabel(new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/geralt.png"))).getImage().getScaledInstance(TILE_SIZE_SELECTION, TILE_SIZE_SELECTION, Image.SCALE_SMOOTH)));
                        break;
                    case 'Y':
                        // Yennefer's character icon
                        label = new JLabel(new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/yen.png"))).getImage().getScaledInstance(TILE_SIZE_SELECTION, TILE_SIZE_SELECTION, Image.SCALE_SMOOTH)));
                        break;
                    case 'g':
                        // Button to select Geralt
                        JButton geraltButton = new JButton("Select Geralt");
                        geraltButton.setPreferredSize(new Dimension(TILE_SIZE_SELECTION, TILE_SIZE_SELECTION));
                        geraltButton.setForeground(Color.WHITE); // Set text color
                        geraltButton.setBackground(new Color(70, 70, 70)); // Set background color
                        geraltButton.setFont(new Font("Arial", Font.BOLD, 16)); // Set font and size
                        geraltButton.setFocusable(false); // Disable focus
                        geraltButton.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(4, 4, 4, 2, Color.BLACK), // Top, Right, Bottom, Left
                                BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Add padding around the button
                        geraltButton.addActionListener(e -> {
                            dispose(); // Close the character selection window
                            // Launch the game with Geralt selected
                            new Game("/images/geralt.png", TILE_SIZE_GAME, GAME_WIDTH, GAME_HEIGHT, Y_OFFSET_GAME);
                        });
                        label = new JLabel();
                        label.setLayout(new BorderLayout());
                        label.add(geraltButton);
                        break;
                    case 'y':
                        // Button to select Yennefer
                        JButton yenButton = new JButton("Select Yennefer");
                        yenButton.setPreferredSize(new Dimension(TILE_SIZE_SELECTION, TILE_SIZE_SELECTION));
                        yenButton.setForeground(Color.WHITE); // Set text color
                        yenButton.setBackground(new Color(70, 70, 70)); // Set background color
                        yenButton.setFont(new Font("Arial", Font.BOLD, 16)); // Set font and size
                        yenButton.setFocusable(false); // Disable focus
                        yenButton.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createMatteBorder(4, 2, 4, 4, Color.BLACK), // Top, Right, Bottom, Left
                                BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Add padding around the button
                        yenButton.addActionListener(e -> {
                            dispose(); // Close the character selection window
                            // Launch the game with Yen selected
                            new Game("/images/yen.png", TILE_SIZE_GAME, GAME_WIDTH, GAME_HEIGHT, Y_OFFSET_GAME);
                        });
                        label = new JLabel();
                        label.setLayout(new BorderLayout());
                        label.add(yenButton);
                        break;
                    default:
                        label = new JLabel();
                        break;
                }
                panel.add(label);
            }
        }

        return panel;
    }

    // Method to load image resources
    private Image loadImage() {
        try {
            return new ImageIcon(Objects.requireNonNull(getClass().getResource("/images/floor.png"))).getImage();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}