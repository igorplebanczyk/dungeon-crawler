import javax.swing.*;
import java.awt.*;

public class CharacterSelection extends JFrame {
    private static final int TILE_SIZE = 60;
    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;
    private static final int Y_OFFSET = 70;

    public CharacterSelection() {
        setTitle("Character Selection");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Get the dimensions of the game window
        int gameWidth = WIDTH * TILE_SIZE;
        int gameHeight = HEIGHT * TILE_SIZE + Y_OFFSET;

        // Set the dimensions of the selection panel to match the game window
        setSize(gameWidth, gameHeight);

        JPanel panel = getPanel();

        add(panel);
        setVisible(true);

        // Center the frame on the screen after setting its size
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
    }

    private JPanel getPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2));

        JButton geraltButton = new JButton("Geralt");
        geraltButton.addActionListener(e -> {
            dispose(); // Close the character selection window
            // Launch the game with Geralt selected
            new Game("/images/geralt.png", TILE_SIZE, WIDTH, HEIGHT, Y_OFFSET);
        });
        panel.add(geraltButton);

        JButton yenButton = new JButton("Yen");
        yenButton.addActionListener(e -> {
            dispose(); // Close the character selection window
            // Launch the game with Yen selected
            new Game("/images/yen.png", TILE_SIZE, WIDTH, HEIGHT, Y_OFFSET);
        });
        panel.add(yenButton);
        return panel;
    }
}
