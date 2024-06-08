import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


public class PauseMenu extends JDialog {
    private final Game game;

    public PauseMenu(JFrame parent) {
        super(parent, "Menu Pauzy", true);
        this.game = (Game) parent;
        setLayout(new FlowLayout());

        JButton resumeButton = new JButton("Wznów");
        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Here you should add the logic to resume the game
                // For example, you could call the `pause()` method of the `Game` class
                // Make sure that the `Game` instance is accessible from this scope
                // Wznowienie gry po naciśnięciu przycisku "Wznów"
                game.pause();
                PauseMenu.this.setVisible(false); // Ukrywamy menu pauzy
            }
        });

        // Dodajemy KeyListener do okna dialogowego
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    // Jeśli naciśnięto ESC, ukrywamy menu pauzy i wznawiamy grę
                    PauseMenu.this.setVisible(false);
                    ((Game)parent).pause();
                }
            }
        });
        this.setFocusable(true);
        this.requestFocusInWindow();
        add(resumeButton);

        JButton quitButton = new JButton("Wyjdź");
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Tutaj dodaj kod do wyjścia z gry
                System.out.println("exiting the game...");
                System.exit(0);
            }
        });
        add(quitButton);

        JButton refreshButton = new JButton("Odśwież");
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("refreshing the game...");
                // Wywołaj metodę generateNewLevel() na instancji gry
                game.generateNewLevel();
                // Następnie wywołaj metodę repaint() na instancji gry
                game.repaint();
            }
        });
        add(refreshButton);

        pack();
        setLocationRelativeTo(parent);
    }
}