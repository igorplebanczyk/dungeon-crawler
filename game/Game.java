package game;

import game.menu.PauseMenu;
import game.object.GameMap;
import game.object.entity.Floor;
import game.object.entity.Player;
import game.object.entity.PlayerCharacter;
import game.ui.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.util.concurrent.CompletableFuture;

public class Game extends JFrame {
    private final PlayerCharacter character;
    private final BufferStrategy bufferStrategy;
    private final Renderer renderer;
    private final GameState state;
    private final Mover mover;
    private Player player;
    private GameMap map;

    public Game(PlayerCharacter character) {
        this.state = new GameState();
        this.character = character;
        this.renderer = new Renderer(this, this.character);
        this.mover = new Mover(this);

        setTitle("Dungeon Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(false);
        setUndecorated(true);

        startLevel(true);

        handleKeyboardInput();
        handleMouseInput();

        pack();
        setSize(Constants.GAME_TILE_NUM * Constants.GAME_TILE_SIZE, Constants.GAME_TILE_NUM * Constants.GAME_TILE_SIZE); // Set the frame size

        // Center the frame on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);

        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();

        repaint();
        setVisible(true);
    }

    private void handleKeyboardInput() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (Game.this.state.isMovementInProgress()) return;

                int key = e.getKeyCode(); // Get the pressed key code
                int dx = 0, dy = 0; // Initialize the change in x and y

                switch (key) {
                    case KeyEvent.VK_W, KeyEvent.VK_UP:
                        dy = -1;
                        break;
                    case KeyEvent.VK_A, KeyEvent.VK_LEFT:
                        dx = -1;
                        break;
                    case KeyEvent.VK_S, KeyEvent.VK_DOWN:
                        dy = 1;
                        break;
                    case KeyEvent.VK_D, KeyEvent.VK_RIGHT:
                        dx = 1;
                        break;
                    case KeyEvent.VK_B:
                        bulldozerMode();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        pause();
                        break;
                }

                Game.this.mover.moveBy(Game.this.player, dx, dy);
            }
        });
    }

    private void handleMouseInput() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (Game.this.state.isMovementInProgress()) return;

                int x = e.getX() / Constants.GAME_TILE_SIZE;
                int y = (e.getY() - Constants.Y_OFFSET) / Constants.GAME_TILE_SIZE;
                Game.this.mover.moveTo(Game.this.player, x, y);
            }
        });
    }

    private void startLevel(boolean initial) {
        if (!initial)
            this.state.getCurrentDungeon().setTile(player.getX(), player.getY(), new Floor()); // Clear the player's previous position

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> this.map = new GameMap(Constants.GAME_TILE_NUM, Constants.GAME_TILE_NUM, Constants.MAP_GRID_SIZE));

        future.thenRun(() -> {
            this.state.setCurrentDungeon(map.getStartingDungeon());
            this.player = new Player(this.state.getCurrentDungeon(), 0, 0, Constants.PLAYER_IMAGE_MAP.get(this.character), this);

            if (initial) {
                this.state.setMessage(new Message("Find Ciri to advance to next level", this));
                this.state.getMessage().display(1500);
            } else {
                this.state.setMessage(new Message("Welcome to level " + this.state.getLevel(), this));
                this.state.getMessage().display(750);
            }
        });
    }

    public void advanceToNextLevel() {
        this.state.incLevel();
        startLevel(false);
        repaint();
    }

    // Toggle bulldozer mode
    private void bulldozerMode() {
        GameState.toggleBulldozerMode();

        if (GameState.isBulldozerMode()) {
            this.state.setMessage(new Message("Bulldozer mode activated ⛏", this));
        } else {
            this.state.setMessage(new Message("Bulldozer mode deactivated ⛏", this));
        }

        this.state.getMessage().display(750);
    }

    public void pause() {
        this.state.togglePause();

        if (this.state.isPaused()) {
            PauseMenu pauseMenu = new PauseMenu(this);
            pauseMenu.setVisible(true);
        }
    }

    // Override the paint method to render directly to the buffer strategy
    @Override
    public void paint(Graphics g) {
        do {
            do {
                Graphics bufferGraphics = bufferStrategy.getDrawGraphics();
                renderer.render(bufferGraphics);
                bufferGraphics.dispose();
            } while (bufferStrategy.contentsRestored());
            bufferStrategy.show();
        } while (bufferStrategy.contentsLost());
    }

    public GameState getGameState() {
        return this.state;
    }

    public Renderer getRenderer() {
        return this.renderer;
    }

    public GameMap getMap() {
        return this.map;
    }
}
