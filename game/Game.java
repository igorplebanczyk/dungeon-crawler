package game;

import game.menu.PauseMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import java.util.List;
import java.util.concurrent.*;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class Game extends JFrame {
    // Game parameters
    private final String characterImage;
    private static final int TILE_SIZE = 60;
    private static final int WIDTH = 15;
    private static final int HEIGHT = 15;
    private static final int Y_OFFSET = 70;
    private static final int GRID_SIZE = 4;

    // Game objects
    private final Dungeon[][] grid;
    private Dungeon dungeon; // Current dungeon
    private Dungeon startingDungeon;
    private Player player;
    private String message;
    private Timer messageTimer; // Timer to clear the message after a certain duration

    private static Map<String, Image> imageCache; // Cache for images
    private final BufferStrategy bufferStrategy; // Buffer strategy for rendering
    private static final Logger LOGGER = Logger.getLogger(Game.class.getName()); // Logger for error messages

    // Game state variables
    private int level = 1;
    private boolean isTimerRunning = false;
    private boolean isPaused = false;
    private static boolean bulldozerMode = false;

    public Game(String characterImage) {
        // Initialize game parameters
        long startTime = System.nanoTime();
        this.characterImage = characterImage;
        grid = new Dungeon[GRID_SIZE][GRID_SIZE];

        // Set up JFrame properties
        setTitle("Dungeon Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(false);

        initializeGame();

        // Add key listener for player movement
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

                // Update player position based on key input
                int newX = player.getX() + dx;
                int newY = player.getY() + dy;

                preventInvalidExit(); // Prevent stepping on an invalid exit

                // Check for valid movement and update player position
                if (newX >= 0 && newX < WIDTH && newY >= 0 && newY < HEIGHT &&
                    (dungeon.getTile(newX, newY) == '.' || dungeon.getTile(newX, newY) == 'E' || dungeon.getTile(newX, newY) == 'D')) {
                    movePlayer(dx, dy, newX, newY);
                }

                // Check for reaching the exit and advance to the next level
                int[] playerPos = player.getPosition();
                if (playerPos[0] == dungeon.getExitX() && playerPos[1] == dungeon.getExitY()) {
                    advanceToNextLevel();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isTimerRunning) return; // If the timer is running, ignore the mouse event

                // Convert mouse coordinates to grid coordinates
                int gridX = e.getX() / TILE_SIZE;
                int gridY = (e.getY() - Y_OFFSET) / TILE_SIZE;

                // Check if the clicked tile is a wall or exit
                if (dungeon.getTile(gridX, gridY) == '#') {
                    showAnnouncement("Can't walk through walls", 500);
                    return;
                }
                else if (dungeon.getTile(gridX, gridY) == 'E') {
                    showAnnouncement("It ain't that easy", 500);
                    return;
                }

                // Use BFS to find the shortest path
                List<Point> path = getPath(gridX, gridY);
                if (path == null) return;
                animateAutoMovement(path); // Animate the player movement
            }
        });

        pack(); // Pack the frame first to calculate its preferred size
        setSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE + Y_OFFSET); // Set the frame size

        // Center the frame on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);

        // Create a buffer strategy
        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();

        setVisible(true);
        repaint();

        long endTime = System.nanoTime();
        System.out.println("Game initialized in " + (endTime - startTime) / 1e6 + "ms");
    }

    public static boolean isBulldozerMode() {
        return bulldozerMode;
    }

    // Call the BFS algorithm to find the shortest path
    private List<Point> getPath(int gridX, int gridY) {
        List<Point> path;
        try {
            path = dungeon.findPath(player.getX(), player.getY(), gridX, gridY);
        } catch (InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof TimeoutException) {
                System.out.println("Pathfinding took too long and was cancelled.");
                return null;
            }
            throw new RuntimeException(ex);
        } catch (TimeoutException ex) { throw new RuntimeException(ex); }
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
                    dungeon.setTile(player.getX(), player.getY(), 'P');

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
        if (dungeon.isDoor(player.getX(), player.getY())) {
            dungeon.setTile(player.getX(), player.getY(), 'D'); // If so, redraw the door
        } else {
            dungeon.setTile(player.getX(), player.getY(), '.'); // Otherwise, redraw the floor
        }
    }

    // Move the player to the new position
    private void movePlayer(int dx, int dy, int targetX, int targetY) {
        redrawPreviousTile(); // Clear the previous player position and redraw either a door or floor

        player.move(dx, dy);
        if (dungeon.isDoor(targetX, targetY)) {
            moveToAdjacentRoom(targetX, targetY);
        }
        dungeon.setTile(player.getX(), player.getY(), 'P'); // Draw the player at the new position
        repaint();
    }

    // Move to the corresponding adjacent dungeon
    private void moveToAdjacentRoom(int newX, int newY) {
        if (newX == 0) {
            dungeon = grid[dungeon.getGridX() - 1][dungeon.getGridY()]; // Left edge
            player.setX(WIDTH - 1);
        } else if (newX == WIDTH - 1) {
            dungeon = grid[dungeon.getGridX() + 1][dungeon.getGridY()]; // Right edge
            player.setX(0);
        } else if (newY == 0) {
            dungeon = grid[dungeon.getGridX()][dungeon.getGridY() - 1]; // Top edge
            player.setY(HEIGHT - 1);
        } else if (newY == HEIGHT - 1) {
            dungeon = grid[dungeon.getGridX()][dungeon.getGridY() + 1]; // Bottom edge
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

    // Advance to the next level
    private void advanceToNextLevel() {
        level++;
        generateNewLevel();
        showAnnouncement("Welcome to level " + level, 750);
        repaint();
    }

    // Prevent being able to exit the dungeon from an invalid position
    private void preventInvalidExit() {
        if (!dungeon.doesHaveExit) {
            dungeon.setExitX(dungeon.getWidth() - 1);
            dungeon.setExitY(dungeon.getHeight() - 1);
            dungeon.setTile(dungeon.getWidth() - 1, dungeon.getHeight() - 1, '#'); // Set invalid exit tile to a wall to prevent an invalid exit
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

    // Preload images into image cache
    private void preloadImages() {
        imageCache = new HashMap<>();
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

    // Load and cache image from file
    private void loadAndCacheImage(String path) {
        try {
            Image image = ImageIO.read(Objects.requireNonNull(getClass().getResource(path)));
            imageCache.put(path, image);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An exception occurred", e);
        }
    }

    // Generate a new level
    private void generateNewLevel() {
        dungeon.setTile(player.getX(), player.getY(), '.'); // Clear the player's previous position
        generateMap(); // Call synchronously, because asynchronous generation is slower in this case
        initializePlayer();
    }

    private void initializeGame() {
        // Generate map asynchronously
        CompletableFuture<Void> future = CompletableFuture.runAsync(this::generateMap);

        // Preload images and initialize player after map generation
        future.thenRun(() -> {
            preloadImages();
            initializePlayer();
        });

        showAnnouncement("Find Ciri to advance to next level", 1500);
    }
    // Initialize the player
    private void initializePlayer() {
        player = new Player(0, 0);
        dungeon.setTile(player.getX(), player.getY(), 'P'); // Set the player's initial position
    }

    // Generate map
    private void generateMap() {
        Random random = new Random();

        createDungeons(random);
        addDoors();
        selectStartingDungeon();
        selectExitDungeon(random);
    }

    // Create dungeons on the grid
    private void createDungeons(Random random) {
        // Create first dungeon at a random position
        createInitialDungeon(random);

        // Create the rest of the dungeons ensuring adjacency, use multithreading
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        createMoreDungeons(random);
        executor.shutdown();

        try {
            boolean tasksEnded = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            if (!tasksEnded) throw new TimeoutException();
        } catch (InterruptedException | TimeoutException e) {
            LOGGER.log(Level.SEVERE, "An exception occurred", e);
        }
    }

    // Populate the grid with more dungeons
    private void createMoreDungeons(Random random) {
        IntStream.range(0, 2).parallel().forEach(_ -> { // Iterate twice to increase the number of dungeons
            IntStream.range(0, GRID_SIZE).parallel().forEach(i -> IntStream.range(0, GRID_SIZE).parallel().forEach(j -> {
                if (grid[i][j] == null && hasAdjacentDungeon(i, j)) {
                    // Randomly decide whether to create a dungeon
                    boolean shouldCreateDungeon = random.nextBoolean();
                    if (shouldCreateDungeon) {
                        grid[i][j] = new Dungeon(WIDTH, HEIGHT, i, j);
                    }
                }
            }));
        });
    }

    // Create the initial dungeon, from which all other dungeons branch out
    private void createInitialDungeon(Random random) {
        int firstDungeonX = random.nextInt(1, 2);
        int firstDungeonY = random.nextInt(1, 2);
        grid[firstDungeonX][firstDungeonY] = new Dungeon(WIDTH, HEIGHT, firstDungeonX, firstDungeonY);
    }

    // Add doors to dungeons
    private void addDoors() {
        // Add doors
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Dungeon d = grid[i][j];
                if (d != null) {
                    if (i > 0 && grid[i - 1][j] != null) { // Check left
                        d.addDoor(0, HEIGHT / 2);
                    }
                    if (j > 0 && grid[i][j - 1] != null) { // Check up
                        d.addDoor(WIDTH / 2, 0);
                    }
                    if (i < GRID_SIZE - 1 && grid[i + 1][j] != null) { // Check right
                        d.addDoor(WIDTH - 1, HEIGHT / 2);
                    }
                    if (j < GRID_SIZE - 1 && grid[i][j + 1] != null) { // Check down
                        d.addDoor(WIDTH / 2, HEIGHT - 1);
                    }
                }
            }
        }
    }

    // Select the starting dungeon
    private void selectStartingDungeon() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] != null) {
                    dungeon = grid[i][j];
                    startingDungeon = dungeon; // Store the starting dungeon
                }
            }
        }
    }

    // Select the exit dungeon
    private void selectExitDungeon(Random random) {
        // Create a list of all dungeons except the starting one
        List<Dungeon> dungeons = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] != null && grid[i][j] != startingDungeon) {
                    dungeons.add(grid[i][j]);
                }
            }
        }

        // Randomly select a dungeon from the list to be the exit dungeon
        Dungeon exitDungeon = dungeons.get(random.nextInt(dungeons.size()));
        exitDungeon.doesHaveExit = true; // Set the exit flag
        exitDungeon.setTile(exitDungeon.getExitX(), exitDungeon.getExitY(), 'E'); // Set the exit in the selected dungeon
    }

    // Check if a dungeons exists at adjacent positions
    private boolean hasAdjacentDungeon(int x, int y) {
        if (x > 0 && grid[x - 1][y] != null) { // Check left
            return true;
        }
        if (y > 0 && grid[x][y - 1] != null) { // Check up
            return true;
        }
        if (x < 3 && grid[x + 1][y] != null) { // Check right
            return true;
        }
        // Check down
        return y < 3 && grid[x][y + 1] != null;
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
                char tile = dungeon.getTile(x, y);
                Image imageToDraw = switch (tile) {
                    case '#' -> getImageFromCache("/images/wall.png");
                    case '.' -> getImageFromCache("/images/floor.png");
                    case 'P' -> getImageFromCache(characterImage);
                    case 'E' -> getImageFromCache("/images/ciri.png");
                    case 'D' -> getImageFromCache("/images/door.png");
                    default -> null;
                };
                if (imageToDraw != null) {
                    g.drawImage(imageToDraw, x * TILE_SIZE, Y_OFFSET - 8 + y * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }
    }

    // Retrieve image from cache
    private Image getImageFromCache(String path) {
        return imageCache.get(path);
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
