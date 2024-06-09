package game;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public abstract class Menu extends JFrame {
    protected static final int TILE_SIZE = 150;
    protected static final int WINDOW_WIDTH = 3;
    protected static final int WINDOW_HEIGHT = 3;

    public Menu() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(WINDOW_WIDTH * TILE_SIZE, WINDOW_HEIGHT * TILE_SIZE);
        getRootPane().setBorder(BorderFactory.createLineBorder(Color.BLACK, 6));
    }

    protected void addFloorTiles(JPanel panel, int tileSize, int width, int height) {
        ImageIcon floorIcon = createScaledIcon("/images/floor.png", tileSize);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                JLabel label = new JLabel(floorIcon);
                label.setPreferredSize(new Dimension(tileSize, tileSize));
                GridBagConstraints gbc = createGridBagConstraints(x, y, 1, 1, 0, 0);
                panel.add(label, gbc);
            }
        }
    }

    protected void addButton(JButton button, JPanel panel, int offsetTop, int offsetBottom, int tileSize, int width, int height) {
        Dimension size = new Dimension(tileSize, tileSize / 2);
        GridBagConstraints gbc = createGridBagConstraints(0, 0, width, height, offsetTop, offsetBottom);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        button.setMaximumSize(size);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 70, 70));
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setFocusable(false);
        button.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, Color.BLACK));
        panel.add(button, gbc);
    }

    protected ImageIcon createScaledIcon(String path, int size) {
        return new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(path))).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    protected GridBagConstraints createGridBagConstraints(int gridX, int gridY, int gridWidth, int gridHeight, int top, int bottom) {
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
