package game;

import game.menu.PauseMenu;
import game.objects.*;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Game extends JFrame {
    // Game constants
    private static final int TILE_SIZE = 45; // Safe to modify; must always be a multiple of 15
    private static final int WIDTH = 15; // Amount of tiles in the x direction
    private static final int HEIGHT = 15; // Amount of tiles in the y direction
    private static final int Y_OFFSET = 70;
    private static final int GRID_SIZE = 2;

    // Game parameters
    private final String characterImage;

    // Game objects
    private GameMap map;
    private Player player;
    private String message;
    private Timer messageTimer; // Timer to clear the message after a certain duration
    private java.util.Map<String, Image> imageCache; // Cache for images
    private final BufferStrategy bufferStrategy;
    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

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

        // Set up JFrame properties
        setTitle("Dungeon Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(false);

        initializeGame();

        handleKeyboardInput(); // Add keylistener to handle keyboard input
        handleMouseInput(); // Add mouselistener to handle mouse input

        pack(); // Pack the frame first to calculate its preferred size
        setSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE + Y_OFFSET); // Set the frame size

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
        if (newX >= 0 && newX < WIDTH && newY >= 0 && newY < HEIGHT &&
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
        int gridX = e.getX() / TILE_SIZE;
        int gridY = (e.getY() - Y_OFFSET) / TILE_SIZE;

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
            player.setX(WIDTH - 1);
        } else if (newX == WIDTH - 1) {
            currentDungeon = map.getGrid()[currentDungeon.getGridX() + 1][currentDungeon.getGridY()]; // Right edge
            player.setX(0);
        } else if (newY == 0) {
            currentDungeon = map.getGrid()[currentDungeon.getGridX()][currentDungeon.getGridY() - 1]; // Top edge
            player.setY(HEIGHT - 1);
        } else if (newY == HEIGHT - 1) {
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
        if (!currentDungeon.doesHaveExit) {
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

        this.map = new GameMap(WIDTH, HEIGHT, GRID_SIZE);
        this.currentDungeon = map.getStartingDungeon();

        initializePlayer();
    }

    private void initializeGame() {
        // Generate map asynchronously
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            this.map = new GameMap(WIDTH, HEIGHT, GRID_SIZE);
            this.currentDungeon = map.getStartingDungeon();
        });

        // Preload images and initialize player after map generation
        future.thenRun(() -> {
            preloadImages();
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
                render(bufferGraphics);
                bufferGraphics.dispose();
            } while (bufferStrategy.contentsRestored());
            bufferStrategy.show();
        } while (bufferStrategy.contentsLost());
    }

    // Render the game
    private void render(Graphics g) {
        drawTopBar(g);
        drawTiles(g);
        if (message != null) drawMessage(g);
    }

    // Draw top bar
    private void drawTopBar(Graphics g) {
        // Draw top bar background
        g.setColor(new Color(50, 50, 50)); // Dark gray color
        g.fillRect(0, 0, getWidth(), Y_OFFSET - 8);

        // Draw level and character name
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times", Font.BOLD, 20));
        g.drawString("Level " + level, 15, Y_OFFSET - 16);
        if (Objects.equals(characterImage, "/images/geralt.png")) {
            g.drawString("Geralt", 825, Y_OFFSET - 16);
        } else if (Objects.equals(characterImage, "/images/yen.png")) {
            g.drawString("Yennefer", 800, Y_OFFSET - 16);
        }
    }

    // Draw a message
    private void drawMessage(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Times ", Font.BOLD, 56));
        FontMetrics fm = g.getFontMetrics();
        int messageWidth = fm.stringWidth(message);
        int messageHeight = fm.getHeight();
        int x = (getWidth() - messageWidth) / 2;
        int y = (getHeight() - messageHeight) / 2 + fm.getAscent();
        g.drawString(message, x, y);
    }

    // Draw tiles based on the dungeon map
    private void drawTiles(Graphics g) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                GameObject tile = currentDungeon.getTile(x, y);
                Image imageToDraw = getImageFromCache(tile.getImagePath());
                if (imageToDraw != null) {
                    g.drawImage(imageToDraw, x * TILE_SIZE, Y_OFFSET - 8 + y * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }
    }

    // Preload images into image cache
    private void preloadImages() {
        if (this.imageCache == null) { // Preload images only if the cache is empty
            this.imageCache = new HashMap<>();
            List<String> imagePaths = List.of(characterImage, "/images/wall.png", "/images/floor.png", "/images/ciri.png", "/images/door.png");

            ExecutorService executor = Executors.newFixedThreadPool(5);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (String path : imagePaths) {
                // Submit a task to load and cache each image
                CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                    loadAndCacheImage(path);
                    return null;
                }, executor);

                futures.add(future);
            }

            // A CompletableFuture that completes when all image loading tasks are done
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allFutures.thenRun(() -> System.out.println("All images preloaded")); // Attach a callback to handle post-loading logic
            executor.shutdown();
        }
    }

    // Load and cache image from file
    private void loadAndCacheImage(String path) {
        try {
            Image image = ImageIO.read(Objects.requireNonNull(getClass().getResource(path)));
            this.imageCache.put(path, image);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An exception occurred", e);
        }
    }

    // Retrieve image from cache
    private Image getImageFromCache(String path) {
        return this.imageCache.get(path);
    }

    // Show announcement message for a specified duration
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
