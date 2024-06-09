package game;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public abstract class Menu extends JFrame {
    public Menu() {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    // Add floor tiles to a panel
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

    // Configure a button with common settings
    protected void configureButton(JButton button, int width, int height, int fontSize, int borderLeft, int borderRight) {
        Dimension buttonSize = new Dimension(width, height);
        button.setPreferredSize(buttonSize);
        button.setMinimumSize(buttonSize);
        button.setMaximumSize(buttonSize);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 70, 70));
        button.setFont(new Font("Arial", Font.BOLD, fontSize));
        button.setFocusable(false);
        button.setBorder(BorderFactory.createMatteBorder(4, borderLeft, 4, borderRight, Color.BLACK));
    }

    protected ImageIcon createScaledIcon(String path, int size) {
        return new ImageIcon(new ImageIcon(Objects.requireNonNull(getClass().getResource(path))).getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH));
    }

    // Creat GridBagConstraints with common settings
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
