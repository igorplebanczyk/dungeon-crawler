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
        JButton resumeButton = new JButton("Resume");
        resumeButton.addActionListener(_ -> {
            game.pause();
            PauseMenu.this.setVisible(false);
        });
        add(resumeButton);

        // Restart button
        JButton refreshButton = new JButton("Restart");
        refreshButton.addActionListener(_ -> {
            if (!Game.isMapGenerating) {
                game.dispose(); // Close the current game
                StartMenu characterSelection = new StartMenu(); // Open the character selection screen
                JPanel characterPanel = characterSelection.getSelectionPanel();
                characterSelection.setContentPane(characterPanel);
                characterPanel.setVisible(true);
            }
        });
        add(refreshButton);

        // Quit button
        JButton quitButton = new JButton("Exit");
        quitButton.addActionListener(_ -> System.exit(0));
        add(quitButton);

        pack();
        setLocationRelativeTo(parent);
    }
}