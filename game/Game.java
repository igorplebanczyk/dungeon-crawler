package game;

import game.menu.PauseMenu;
import game.object.*;
import game.object.entity.*;
import game.ui.Message;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.List;
import java.util.concurrent.*;

public class Game extends JFrame {
    // Game parameters
    private final String characterImage;

    // Game objects
    private GameMap map;
    private Player player;
    private final ImageCache imageCache = new ImageCache();
    private final BufferStrategy bufferStrategy;
    private final Renderer renderer;
    private final GameState state;

    public Game(String characterImage) {
        this.state = new GameState();
        
        // Initialize game parameters
        long startTime = System.nanoTime(); // Start time for measuring initialization time
        this.characterImage = characterImage;
        this.renderer = new Renderer(this, this.characterImage, this.imageCache);

        // Set up JFrame properties
        setTitle("Dungeon Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(false);

        startLevel(true);

        handleKeyboardInput(); // Add keylistener to handle keyboard input
        handleMouseInput(); // Add mouselistener to handle mouse input

        pack(); // Pack the frame first to calculate its preferred size
        setSize(Constants.GAME_TILE_NUM * Constants.GAME_TILE_SIZE, Constants.GAME_TILE_NUM * Constants.GAME_TILE_SIZE + Constants.Y_OFFSET); // Set the frame size

        // Center the frame on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);

        // Create a buffer strategy for rendering
        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();

        repaint();
        setVisible(true);

        long endTime = System.nanoTime(); // End time for measuring initialization time
        System.out.println("Game initialized in " + (endTime - startTime) / 1e6 + "ms");
    }

    // Add a keyListener to handle player movement
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
                        toggleBulldozerMode();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        pause();
                        break;
                }

                handleManualMovement(dx, dy);
            }
        });
    }

    // Handle movement with keyboard input
    private void handleManualMovement(int dx, int dy) {
        // Calculate the new player position
        int newX = player.getX() + dx;
        int newY = player.getY() + dy;

        // Check for valid movement and update player position
        if (newX >= 0 && newX < Constants.GAME_TILE_NUM && newY >= 0 && newY < Constants.GAME_TILE_NUM &&
                (this.state.getCurrentDungeon().getTile(newX, newY).getType() == EntityType.FLOOR || this.state.getCurrentDungeon().getTile(newX, newY).getType() == EntityType.EXIT || this.state.getCurrentDungeon().getTile(newX, newY).getType() == EntityType.DOOR)) {
            player.move(dx, dy);
        }

        // Check for reaching the exit and advance to the next level
        if (player.getX() == this.state.getCurrentDungeon().getExitX() && player.getY() == this.state.getCurrentDungeon().getExitY()) {
            advanceToNextLevel();
        }
    }

    // Add a mouseListener and handle player movement with pathfinding
    private void handleMouseInput() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (Game.this.state.isMovementInProgress()) return; // If the timer is running, ignore the mouse event
                handleAutoMovement(e);
            }
        });
    }

    // Handle movement with pathfinder
    private void handleAutoMovement(MouseEvent e) {
        // Convert mouse coordinates to grid coordinates
        int gridX = e.getX() / Constants.GAME_TILE_SIZE;
        int gridY = (e.getY() - Constants.Y_OFFSET) / Constants.GAME_TILE_SIZE;

        if (this.state.getCurrentDungeon().getTile(gridX, gridY).getType() != EntityType.FLOOR) { // Cannot move to non-floor tiles
            this.state.setMessage(new Message("It ain't that easy", this));
            this.state.getMessage().display(750);
            return;
        }

        List<Point> path = this.state.getCurrentDungeon().findPath(player.getX(), player.getY(), gridX, gridY);
        if (path == null) return;
        animateAutoMovement(path);
    }

    private void animateAutoMovement(List<Point> path) {
        Timer timer = new Timer(Constants.GAME_AUTO_MOVEMENT_DELAY, null);
        this.state.setMovementInProgress(true);

        timer.addActionListener(new ActionListener() {
            int index = 1; // Start at 1 to skip the player's current position

            @Override
            public void actionPerformed(ActionEvent event) {
                if (index < path.size()) {
                    Point position = path.get(index);

                    player.move(position.x - player.getX(), position.y - player.getY());

                    index++;
                } else {
                    // Stop the timer when the player has reached the destination
                    timer.stop();
                    Game.this.state.setMovementInProgress(false);
                }
            }

        });
        timer.start();
    }

    // Toggle bulldozer mode
    private void toggleBulldozerMode() {
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

    private void startLevel(boolean initial) {
        if (!initial) this.state.getCurrentDungeon().setTile(player.getX(), player.getY(), new Floor()); // Clear the player's previous position

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            this.map = new GameMap(Constants.GAME_TILE_NUM, Constants.GAME_TILE_NUM, Constants.MAP_GRID_SIZE);
            this.state.setCurrentDungeon(map.getStartingDungeon());
            if (initial) imageCache.cacheImages(characterImage);
        });

        future.thenRun(() -> {
            this.player = new Player(this.state.getCurrentDungeon(), 0, 0, characterImage, this);

            if (initial) {
                this.state.setMessage(new Message("Find Ciri to advance to next level", this));
                this.state.getMessage().display(1500);
            } else {
                this.state.setMessage(new Message("Welcome to level " + this.state.getLevel(), this));
                this.state.getMessage().display(750);
            }
        });
    }

    private void advanceToNextLevel() {
        this.state.incLevel();
        startLevel(false);
        repaint();
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
