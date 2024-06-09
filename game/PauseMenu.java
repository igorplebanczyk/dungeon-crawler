package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PauseMenu extends JDialog {
    private final Game game;

    public PauseMenu(JFrame parent) {
        super(parent, "Pause Menu", true);
        this.game = (Game) parent;
        setLayout(new FlowLayout());

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

        // Resume button
        JButton resumeButton = getResumeButton();
        add(resumeButton);

        // Restart button
        JButton refreshButton = getRefreshButton();
        add(refreshButton);

        // Quit button
        JButton quitButton = getQuitButton();
        add(quitButton);

        pack();
        setLocationRelativeTo(parent);
    }

    private static JButton getQuitButton() {
        JButton quitButton = new JButton("Exit");
        quitButton.addActionListener(_ -> System.exit(0));
        return quitButton;
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
}