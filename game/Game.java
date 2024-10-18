package game;

import game.menu.PauseMenu;
import game.objects.*;

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
    private String message;
    private Timer messageTimer; // Timer to clear the message after a certain duration
    private final ImageCache imageCache = new ImageCache();
    private final BufferStrategy bufferStrategy;
    private final Renderer renderer;

    // Game state variables
    private Dungeon currentDungeon;
    private int level = 1;
    private boolean isTimerRunning = false;
    private boolean isPaused = false;
    private static boolean bulldozerMode = false;

    public Game(String characterImage) {
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
                if (isTimerRunning) return; // If the timer is running, ignore the keyboard press

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
                (currentDungeon.getTile(newX, newY).getType() == GameObjectType.FLOOR || currentDungeon.getTile(newX, newY).getType() == GameObjectType.EXIT || currentDungeon.getTile(newX, newY).getType() == GameObjectType.DOOR)) {
            movePlayer(dx, dy, newX, newY);
        }

        // Check for reaching the exit and advance to the next level
        if (player.getX() == currentDungeon.getExitX() && player.getY() == currentDungeon.getExitY()) {
            advanceToNextLevel();
        }
    }

    // Add a mouseListener and handle player movement with pathfinding
    private void handleMouseInput() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isTimerRunning) return; // If the timer is running, ignore the mouse event
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
        if (currentDungeon.getTile(gridX, gridY).getType() == GameObjectType.WALL) {
            showAnnouncement("Can't walk through walls", 500);
            return;
        } else if (currentDungeon.getTile(gridX, gridY).getType() == GameObjectType.EXIT) {
            showAnnouncement("It ain't that easy", 500);
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
        try {
            path = currentDungeon.findPath(player.getX(), player.getY(), gridX, gridY);
        } catch (InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof TimeoutException) {
                System.out.println("Pathfinding took too long and was cancelled.");
                return null;
            }
            throw new RuntimeException(ex);
        } catch (TimeoutException ex) {
            throw new RuntimeException(ex);
        }
        return path;
    }

    // Animate the auto player movement
    private void animateAutoMovement(List<Point> path) {
        // Create a Timer to animate the movement
        Timer timer = new Timer(150, null); // 150ms delay between each move
        isTimerRunning = true; // Set the timer running flag
        timer.addActionListener(new ActionListener() {
            int index = 0;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (index < path.size()) {
                    redrawPreviousTile(); // Clear the previous player position and redraw either a door or floor

                    // Update the player's position
                    Point position = path.get(index);
                    player.move(position.x - player.getX(), position.y - player.getY());

                    // Set the new player position
                    currentDungeon.setTile(player.getX(), player.getY(), player);

                    Game.this.revalidate(); // Re-layout the components
                    Game.this.repaint(); // Refresh the screen
                    index++;
                } else {
                    // Stop the timer when the player has reached the destination
                    timer.stop();
                    isTimerRunning = false;
                }
            }

        });
        timer.start();
    }

    private void redrawPreviousTile() {
        if (currentDungeon.isDoor(player.getX(), player.getY())) {
            currentDungeon.setTile(player.getX(), player.getY(), new Door()); // If so, redraw the door
        } else {
            currentDungeon.setTile(player.getX(), player.getY(), new Floor()); // Otherwise, redraw the floor
        }
    }

    // Move the player to the new position
    private void movePlayer(int dx, int dy, int targetX, int targetY) {
        redrawPreviousTile(); // Clear the previous player position and redraw either a door or floor

        player.move(dx, dy);
        if (currentDungeon.isDoor(targetX, targetY)) {
            moveToAdjacentRoom(targetX, targetY);
        }
        currentDungeon.setTile(player.getX(), player.getY(), this.player); // Draw the player at the new position
        repaint();
    }

    // Move to the corresponding adjacent dungeon
    private void moveToAdjacentRoom(int newX, int newY) {
        if (newX == 0) {
            currentDungeon = map.getGrid()[currentDungeon.getGridX() - 1][currentDungeon.getGridY()]; // Left edge
            player.setX(Constants.GAME_TILE_NUM - 1);
        } else if (newX == Constants.GAME_TILE_NUM - 1) {
            currentDungeon = map.getGrid()[currentDungeon.getGridX() + 1][currentDungeon.getGridY()]; // Right edge
            player.setX(0);
        } else if (newY == 0) {
            currentDungeon = map.getGrid()[currentDungeon.getGridX()][currentDungeon.getGridY() - 1]; // Top edge
            player.setY(Constants.GAME_TILE_NUM - 1);
        } else if (newY == Constants.GAME_TILE_NUM - 1) {
            currentDungeon = map.getGrid()[currentDungeon.getGridX()][currentDungeon.getGridY() + 1]; // Bottom edge
            player.setY(0);
        }
    }

    // Toggle bulldozer mode
    private void toggleBulldozerMode() {
        if (isBulldozerMode()) {
            showAnnouncement("Bulldozer mode deactivated ⛏", 750);
            bulldozerMode = false;
        } else {
            showAnnouncement("Bulldozer mode activated ⛏", 750);
            bulldozerMode = true;
        }
    }

    public static boolean isBulldozerMode() {
        return bulldozerMode;
    }

    // Advance to the next level
    private void advanceToNextLevel() {
        level++;
        generateNewLevel();
        showAnnouncement("Welcome to level " + level, 750);
        repaint();
    }

    // Prevent being able to exit the dungeon from an invalid position
    private void preventInvalidExit() {
        if (!currentDungeon.doesHaveExit()) {
            currentDungeon.setExitX(currentDungeon.getWidth() - 1);
            currentDungeon.setExitY(currentDungeon.getHeight() - 1);
            currentDungeon.setTile(currentDungeon.getWidth() - 1, currentDungeon.getHeight() - 1, new Wall()); // Set invalid exit tile to a wall to prevent an invalid exit
        }
    }

    // Pause the game
    public void pause() {
        isPaused = !isPaused; // Toggle the paused state

        if (isPaused) {
            PauseMenu pauseMenu = new PauseMenu(this);
            pauseMenu.setVisible(true);
        }
    }

    // Generate a new level
    private void generateNewLevel() {
        currentDungeon.setTile(player.getX(), player.getY(), new Floor());// Clear the player's previous position

        this.map = new GameMap(Constants.GAME_TILE_NUM, Constants.GAME_TILE_NUM, Constants.MAP_GRID_SIZE);
        this.currentDungeon = map.getStartingDungeon();

        initializePlayer();
    }

    private void initializeGame() {
        // Generate map asynchronously
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            this.map = new GameMap(Constants.GAME_TILE_NUM, Constants.GAME_TILE_NUM, Constants.MAP_GRID_SIZE);
            this.currentDungeon = map.getStartingDungeon();
        });

        // Preload images and initialize player after map generation
        future.thenRun(() -> {
            imageCache.cacheImages(characterImage);
            initializePlayer();
        });

        showAnnouncement("Find Ciri to advance to next level", 1500);
    }

    // Initialize the player
    private void initializePlayer() {
        player = new Player(0, 0, characterImage);
        currentDungeon.setTile(player.getX(), player.getY(), this.player); // Set the player's initial position
    }

    // Override the paint method to render directly to the buffer strategy
    @Override
    public void paint(Graphics g) {
        do {
            do {
                Graphics bufferGraphics = bufferStrategy.getDrawGraphics();
                renderer.render(bufferGraphics, this.level, this.currentDungeon, this.message);
                bufferGraphics.dispose();
            } while (bufferStrategy.contentsRestored());
            bufferStrategy.show();
        } while (bufferStrategy.contentsLost());
    }

    private void showAnnouncement(String message, int duration) {
        this.message = message; // Set the message
        if (messageTimer != null) {
            messageTimer.stop();
        }
        messageTimer = new Timer(duration, _ -> {
            this.message = null; // Clear the message after the duration
            repaint();
        });
        messageTimer.setRepeats(false);
        messageTimer.start();
        repaint();
    }
}
