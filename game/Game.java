package game;

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

public class Game extends JFrame {
    // Game parameters
    private final String characterImage;
    private final int TILE_SIZE ;
    private final int WIDTH;
    private final int HEIGHT;
    private final int Y_OFFSET;
    private final int GRID_SIZE = 4;

    // Game objects
    private final Dungeon[][] grid;
    private Dungeon dungeon;
    private Dungeon startingDungeon;
    private Player player;
    private final BufferStrategy bufferStrategy;
    private Map<String, Image> imageCache;
    private String message;
    private Timer messageTimer;
    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

    // Game state variables
    private int level = 1;
    private boolean isTimerRunning = false;
    public static boolean bulldozerMode = false;
    public boolean isPaused = false;
    public static volatile boolean isMapGenerating = false;

    public Game(String characterImage, int TILE_SIZE, int WIDTH, int HEIGHT, int Y_OFFSET) {
        // Initialize game parameters
        this.characterImage = characterImage;
        this.TILE_SIZE = TILE_SIZE;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.Y_OFFSET = Y_OFFSET;

        // Game grid
        grid = new Dungeon[GRID_SIZE][GRID_SIZE];
        generateMap();
        preloadImages();
        initializeGame();

        // Set up JFrame properties
        setTitle("Dungeon Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(false);

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
                if (playerPos[0] == dungeon.exitX && playerPos[1] == dungeon.exitY) {
                    advanceToNextLevel();
                }
            }
        });

        this.addMouseListener(new MouseAdapter() {
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

        pack(); // pack the frame first to calculate its preferred size
        int frameWidth = WIDTH * TILE_SIZE; // 20 pixels padding on each side
        int frameHeight = HEIGHT * TILE_SIZE + Y_OFFSET; // 20 pixels padding on top and bottom
        setSize(frameWidth, frameHeight);

        // Center the frame on the screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);

        setVisible(true);
        createBufferStrategy(2);
        bufferStrategy = getBufferStrategy();
    }

    private List<Point> getPath(int gridX, int gridY) {
        List<Point> path;
        try {
            path = dungeon.bfs(player.getX(), player.getY(), gridX, gridY);
        } catch (InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof TimeoutException) {
                System.out.println("Pathfinding took too long and was cancelled.");
                return null;
            }
            throw new RuntimeException(ex);
        } catch (TimeoutException ex) { throw new RuntimeException(ex);}
        return path;
    }

    private void animateAutoMovement(List<Point> path) {
        // Create a Timer to animate the movement
        Timer timer = new Timer(150, null); // 150ms delay between each move
        isTimerRunning = true; // Set the timer running flag
        timer.addActionListener(new ActionListener() {
            int index = 0;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (index < path.size()) {
                    // Clear the previous player position
                    dungeon.setTile(player.getX(), player.getY(), '.');

                    // Update the player's position
                    Point position = path.get(index);
                    player.setX(position.x);
                    player.setY(position.y);

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

    private void movePlayer(int dx, int dy, int newX, int newY) {
        // Check if the player is currently on a door
        if (dungeon.isDoor(player.getX(), player.getY())) {
            dungeon.setTile(player.getX(), player.getY(), 'D'); // If so, redraw the door
        } else {
            dungeon.setTile(player.getX(), player.getY(), '.'); // Otherwise, redraw the floor
        }

        player.move(dx, dy);
        if (dungeon.isDoor(newX, newY)) {
            moveToAdjacentRoom(newX, newY);
        }
        dungeon.setTile(player.getX(), player.getY(), 'P'); // Draw the player at the new position
        repaint();
    }

    // Move to the corresponding adjacent dungeon
    private void moveToAdjacentRoom(int newX, int newY) {
        if (newX == 0) { // Left edge
            dungeon = grid[dungeon.getGridX() - 1][dungeon.getGridY()];
            player.setX(WIDTH - 1);
        } else if (newX == WIDTH - 1) { // Right edge
            dungeon = grid[dungeon.getGridX() + 1][dungeon.getGridY()];
            player.setX(0);
        } else if (newY == 0) { // Top edge
            dungeon = grid[dungeon.getGridX()][dungeon.getGridY() - 1];
            player.setY(HEIGHT - 1);
        } else if (newY == HEIGHT - 1) { // Bottom edge
            dungeon = grid[dungeon.getGridX()][dungeon.getGridY() + 1];
            player.setY(0);
        }
    }

    // Toggle bulldozer mode
    private void toggleBulldozerMode() {
        if (!bulldozerMode) {
            showAnnouncement("Bulldozer mode activated ⛏", 750);
            bulldozerMode = true;
        } else {
            showAnnouncement("Bulldozer mode deactivated ⛏", 750);
            bulldozerMode = false;
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
            dungeon.exitX = dungeon.width - 1;
            dungeon.exitY = dungeon.height - 1;
            dungeon.setTile(dungeon.width - 1, dungeon.height - 1, '#'); // Set invalid exit tile to a wall to prevent an invalid exit
        }
    }

    // Pause the game
    public void pause() {
        isPaused = !isPaused; // Toggle the paused state

        if (isPaused) {
            System.out.println("Game paused");
            PauseMenu pauseMenu = new PauseMenu(this);
            pauseMenu.setVisible(true);
        } else {
            System.out.println("Game resumed");
        }
    }

    // Preload images into image cache
    private void preloadImages() {
        imageCache = new HashMap<>();
        loadAndCacheImage(characterImage); // Load the provided character image
        loadAndCacheImage("/images/wall.png");
        loadAndCacheImage("/images/floor.png");
        loadAndCacheImage("/images/ciri.png");
        loadAndCacheImage("/images/door.png");
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
        generateMap();
        initializeGame();
    }

    // Initialize game state
    private void initializeGame() {
        startingDungeon.map[0][0] = 'P';
        player = new Player(0, 0);
        dungeon.setTile(player.getX(), player.getY(), 'P');
        showAnnouncement("Find Ciri to advance to next level", 1500);
    }

    // Generate map
    private void generateMap() {
        isMapGenerating = true;
        Random random = new Random();

        // Create first dungeon at a random position
        int firstDungeonX = random.nextInt(1, 2);
        int firstDungeonY = random.nextInt(1, 2);
        grid[firstDungeonX][firstDungeonY] = new Dungeon(WIDTH, HEIGHT, firstDungeonX, firstDungeonY);

        // Create the rest of the dungeons ensuring adjacency
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int k = 0; k < 2; k++) { // Iterate twice to increase the number of dungeons
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    int finalI = i;
                    int finalJ = j;
                    executor.submit(() -> {
                        if (grid[finalI][finalJ] == null && hasAdjacentDungeon(finalI, finalJ)) {
                            // Randomly decide whether to create a dungeon
                            boolean shouldCreateDungeon = random.nextBoolean();
                            if (shouldCreateDungeon) {
                                grid[finalI][finalJ] = new Dungeon(WIDTH, HEIGHT, finalI, finalJ);
                            }
                        }
                    });
                }
            }
        }
        executor.shutdown();

        try {
            boolean tasksEnded = executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            if (!tasksEnded) throw new TimeoutException();
        } catch (InterruptedException | TimeoutException e) {
            LOGGER.log(Level.SEVERE, "An exception occurred", e);
        }

        addDoors();
        selectStartingDungeon();
        selectExitDungeon(random);
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
        exitDungeon.map[exitDungeon.exitY][exitDungeon.exitX] = 'E'; // Set the exit in the selected dungeon
        isMapGenerating = false;
    }

    // Check if a dungeon exists at adjacent positions
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
        drawTiles(g);
        drawMessageIfNecessary(g);
        drawTopBar(g);
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

    // Draw message if present
    private void drawMessageIfNecessary(Graphics g) {
        if (message != null) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Times ", Font.BOLD, 56));
            FontMetrics fm = g.getFontMetrics();
            int messageWidth = fm.stringWidth(message);
            int messageHeight = fm.getHeight();
            int x = (getWidth() - messageWidth) / 2;
            int y = (getHeight() - messageHeight) / 2 + fm.getAscent();
            g.drawString(message, x, y);
        }
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
