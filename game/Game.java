package game;

import game.menu.PauseMenu;
import game.objects.*;
import game.ui.ImageCache;
import game.ui.Message;
import game.ui.Renderer;

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
    private final game.ui.Renderer renderer;

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

        initializeGame();

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

        preventInvalidExit(); // Prevent stepping on an invalid exit by replacing it with a wall

        // Check for valid movement and update player position
        if (newX >= 0 && newX < Constants.GAME_TILE_NUM && newY >= 0 && newY < Constants.GAME_TILE_NUM &&
                (this.state.getCurrentDungeon().getTile(newX, newY).getType() == GameObjectType.FLOOR || this.state.getCurrentDungeon().getTile(newX, newY).getType() == GameObjectType.EXIT || this.state.getCurrentDungeon().getTile(newX, newY).getType() == GameObjectType.DOOR)) {
            movePlayer(dx, dy, newX, newY);
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

        // Check if the clicked tile is a wall or exit
        if (this.state.getCurrentDungeon().getTile(gridX, gridY).getType() == GameObjectType.WALL) {
            this.state.setMessage(new Message("Can't go through walls", this));
            this.state.getMessage().display(750);
            return;
        } else if (this.state.getCurrentDungeon().getTile(gridX, gridY).getType() == GameObjectType.EXIT || this.state.getCurrentDungeon().getTile(gridX, gridY).getType() == GameObjectType.DOOR) {
            this.state.setMessage(new Message("It ain't that easy", this));
            this.state.getMessage().display(750);
            return;
        }

        // Use BFS to find the shortest path
        List<Point> path = getAutoMovementPath(gridX, gridY);
        if (path == null) return;
        animateAutoMovement(path); // Animate the player movement
    }

    // Call the BFS algorithm to find the shortest path
    private List<Point> getAutoMovementPath(int gridX, int gridY) {
        List<Point> path;
        path = this.state.getCurrentDungeon().findPath(player.getX(), player.getY(), gridX, gridY);
        return path;
    }

    // Animate the auto player movement
    private void animateAutoMovement(List<Point> path) {
        // Create a Timer to animate the movement
        Timer timer = new Timer(150, null); // 150ms delay between each move
        this.state.setMovementInProgress(true);
        timer.addActionListener(new ActionListener() {
            int index = 0;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (index < path.size()) {
                    redrawPreviousTile(); // Clear the previous player position and redraw either a door or floor

                    // Update the player's position
                    Point position = path.get(index);
                    Game.this.player.move(position.x - player.getX(), position.y - player.getY());

                    // Set the new player position
                    Game.this.state.getCurrentDungeon().setTile(player.getX(), player.getY(), player);

                    Game.this.revalidate(); // Re-layout the components
                    Game.this.repaint(); // Refresh the screen
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

    private void redrawPreviousTile() {
        if (this.state.getCurrentDungeon().isDoor(player.getX(), player.getY())) {
            this.state.getCurrentDungeon().setTile(player.getX(), player.getY(), new Door()); // If so, redraw the door
        } else {
            this.state.getCurrentDungeon().setTile(player.getX(), player.getY(), new Floor()); // Otherwise, redraw the floor
        }
    }

    // Move the player to the new position
    private void movePlayer(int dx, int dy, int targetX, int targetY) {
        redrawPreviousTile(); // Clear the previous player position and redraw either a door or floor

        player.move(dx, dy);
        if (this.state.getCurrentDungeon().isDoor(targetX, targetY)) {
            moveToAdjacentRoom(targetX, targetY);
        }
        this.state.getCurrentDungeon().setTile(player.getX(), player.getY(), this.player); // Draw the player at the new position
        repaint();
    }

    // Move to the corresponding adjacent dungeon
    private void moveToAdjacentRoom(int newX, int newY) {
        if (newX == 0) {
            this.state.setCurrentDungeon(map.getGrid()[this.state.getCurrentDungeon().getGridX() - 1][this.state.getCurrentDungeon().getGridY()]); // Left edge
            player.setX(Constants.GAME_TILE_NUM - 1);
        } else if (newX == Constants.GAME_TILE_NUM - 1) {
            this.state.setCurrentDungeon(map.getGrid()[this.state.getCurrentDungeon().getGridX() + 1][this.state.getCurrentDungeon().getGridY()]); // Right edge
            player.setX(0);
        } else if (newY == 0) {
            this.state.setCurrentDungeon(map.getGrid()[this.state.getCurrentDungeon().getGridX()][this.state.getCurrentDungeon().getGridY() - 1]); // Top edge
            player.setY(Constants.GAME_TILE_NUM - 1);
        } else if (newY == Constants.GAME_TILE_NUM - 1) {
            this.state.setCurrentDungeon(map.getGrid()[this.state.getCurrentDungeon().getGridX()][this.state.getCurrentDungeon().getGridY() + 1]); // Bottom edge
            player.setY(0);
        }
    }

    // Toggle bulldozer mode
    private void toggleBulldozerMode() {
        if (GameState.isBulldozerMode()) {
            this.state.setMessage(new Message("Bulldozer mode deactivated ⛏", this));
            this.state.getMessage().display(750);
            GameState.setBulldozerMode(false);
        } else {
            this.state.setMessage(new Message("Bulldozer mode activated ⛏", this));
            this.state.getMessage().display(750);
            GameState.setBulldozerMode(true);
        }
    }

    // Advance to the next level
    private void advanceToNextLevel() {
        this.state.incLevel();
        generateNewLevel();
        this.state.setMessage(new Message("Welcome to level " + this.state.getLevel(), this));
        this.state.getMessage().display(750);
        repaint();
    }

    // Prevent being able to exit the dungeon from an invalid position
    private void preventInvalidExit() {
        if (!this.state.getCurrentDungeon().doesHaveExit()) {
            this.state.getCurrentDungeon().setExitX(this.state.getCurrentDungeon().getWidth() - 1);
            this.state.getCurrentDungeon().setExitY(this.state.getCurrentDungeon().getHeight() - 1);
            this.state.getCurrentDungeon().setTile(this.state.getCurrentDungeon().getWidth() - 1, this.state.getCurrentDungeon().getHeight() - 1, new Wall()); // Set invalid exit tile to a wall to prevent an invalid exit
        }
    }

    // Pause the game
    public void pause() {
        this.state.togglePause(); // Toggle the paused this.state

        if (this.state.isPaused()) {
            PauseMenu pauseMenu = new PauseMenu(this);
            pauseMenu.setVisible(true);
        }
    }

    // Generate a new level
    private void generateNewLevel() {
        this.state.getCurrentDungeon().setTile(player.getX(), player.getY(), new Floor());// Clear the player's previous position

        this.map = new GameMap(Constants.GAME_TILE_NUM, Constants.GAME_TILE_NUM, Constants.MAP_GRID_SIZE);
        this.state.setCurrentDungeon(map.getStartingDungeon());

        this.player = new Player(this.state.getCurrentDungeon(), 0, 0, characterImage);
    }

    private void initializeGame() {
        // Generate map asynchronously
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            this.map = new GameMap(Constants.GAME_TILE_NUM, Constants.GAME_TILE_NUM, Constants.MAP_GRID_SIZE);
            this.state.setCurrentDungeon(map.getStartingDungeon());
        });

        // Preload images and initialize player after map generation
        future.thenRun(() -> {
            imageCache.cacheImages(characterImage);
            this.player = new Player(this.state.getCurrentDungeon(), 0, 0, characterImage);
        });

        this.state.setMessage(new Message("Find Ciri to advance to next level", this));
        this.state.getMessage().display(1500);
    }

    // Override the paint method to render directly to the buffer strategy
    @Override
    public void paint(Graphics g) {
        do {
            do {
                Graphics bufferGraphics = bufferStrategy.getDrawGraphics();
                renderer.render(bufferGraphics, this.state.getLevel(), this.state.getCurrentDungeon(), this.state.getMessage().getText());
                bufferGraphics.dispose();
            } while (bufferStrategy.contentsRestored());
            bufferStrategy.show();
        } while (bufferStrategy.contentsLost());
    }
}
