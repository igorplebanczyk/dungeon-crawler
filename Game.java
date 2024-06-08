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
import java.awt.event.KeyListener;

public class Game extends JFrame {
    // Variables to store game parameters
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

    // Game state variables
    private int level = 1;
    private Map<String, Image> imageCache;
    private String message;
    private Timer messageTimer;
    private boolean isTimerRunning = false;
    public static boolean bulldozerMode = false;

    private boolean isPaused = false;

    public void pause() {
        isPaused = !isPaused; // Zmieniamy stan gry na przeciwny

        if (isPaused) {
            System.out.println("Game paused");
            // Tutaj zatrzymaj logikę gry i wyświetl menu pauzy
            // Możesz na przykład zmienić stan gry na "PAUSED" i wywołać metodę repaint(), aby odświeżyć ekran
            PauseMenu pauseMenu = new PauseMenu(this);
            pauseMenu.setVisible(true);
        } else {
            System.out.println("Game resumed");

            // Tutaj wznow logikę gry
            // Możesz na przykład zmienić stan gry na "RUNNING" i wywołać metodę repaint(), aby odświeżyć ekran
        }
    }

    public Game(String characterImage, int TILE_SIZE, int WIDTH, int HEIGHT, int Y_OFFSET) {
        // Initialize game parameters
        this.characterImage = characterImage;
        this.TILE_SIZE = TILE_SIZE;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.Y_OFFSET = Y_OFFSET;

        //Game grid
        grid = new Dungeon[GRID_SIZE][GRID_SIZE];
        generateMap(); // Generate the map
        preloadImages(); // Preload images
        initializeGame(); // Initialize the game

        // Set up JFrame properties
        setTitle("Dungeon Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setIgnoreRepaint(true);
        setResizable(false);

        // Add key listener for player movement
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isTimerRunning) {
                    return; // If the timer is running, ignore the mouse event
                }

                int key = e.getKeyCode();
                int dx = 0, dy = 0;

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
                        if (!bulldozerMode) {
                            showAnnouncement("Bulldozer mode activated ⛏", 750);
                            bulldozerMode = true;
                        } else {
                            showAnnouncement("Bulldozer mode deactivated ⛏", 750);
                            bulldozerMode = false;
                        }
                        break;
                    case KeyEvent.VK_ESCAPE:
                        pause(); // Metoda, która zatrzymuje grę i wyświetla menu pauzy
                        break;
                }

                // Update player position based on key input
                int newX = player.getX() + dx;
                int newY = player.getY() + dy;

                if (!dungeon.doesHaveExit) {
                    dungeon.exitX = dungeon.width - 1;
                    dungeon.exitY = dungeon.height - 1;
                    dungeon.setTile(dungeon.width - 1, dungeon.height - 1, '#'); // Set invalid exit tile to a wall to prevent an invalid exit
                }

                // Check for valid movement and update player position
                if (newX >= 0 && newX < WIDTH && newY >= 0 && newY < HEIGHT &&
                    (dungeon.getTile(newX, newY) == '.' || dungeon.getTile(newX, newY) == 'E' || dungeon.getTile(newX, newY) == 'D')) {
                    // Check if the player is currently on a door
                    if (dungeon.isDoor(player.getX(), player.getY())) {
                        // If so, redraw the door
                        dungeon.setTile(player.getX(), player.getY(), 'D');
                    } else {
                        // Otherwise, redraw the floor tile
                        dungeon.setTile(player.getX(), player.getY(), '.');
                    }

                    player.move(dx, dy); // Move the player first
                    if (dungeon.isDoor(newX, newY)) { // Check if the new position is a door
                        // Check which edge the door is on and move to the corresponding adjacent dungeon
                        if (newX == 0) { // Left edge
                            dungeon = grid[dungeon.getX() - 1][dungeon.getY()];
                            player.setX(WIDTH - 1);
                        } else if (newX == WIDTH - 1) { // Right edge
                            dungeon = grid[dungeon.getX() + 1][dungeon.getY()];
                            player.setX(0);
                        } else if (newY == 0) { // Top edge
                            dungeon = grid[dungeon.getX()][dungeon.getY() - 1];
                            player.setY(HEIGHT - 1);
                        } else if (newY == HEIGHT - 1) { // Bottom edge
                            dungeon = grid[dungeon.getX()][dungeon.getY() + 1];
                            player.setY(0);
                        }
                    }
                    dungeon.setTile(player.getX(), player.getY(), 'P'); // Draw the player at the new position
                    repaint();
                }

                // Check for reaching the exit and advance to the next level
                int[] playerPos = player.getPosition();
                if (playerPos[0] == dungeon.exitX && playerPos[1] == dungeon.exitY) {
                    level++;
                    generateNewLevel();
                    showAnnouncement("Welcome to level " + level, 750);
                    repaint();
                }
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isTimerRunning) {
                    return; // If the timer is running, ignore the mouse event
                }
                // Convert mouse coordinates to grid coordinates
                int gridX = e.getX() / TILE_SIZE;
                int gridY = (e.getY() - Y_OFFSET) / TILE_SIZE;

                // Check if the clicked tile is a wall
                if (dungeon.getTile(gridX, gridY) == '#') {
                    showAnnouncement("Can't walk through walls", 500);
                    return;
                }
                else if (dungeon.getTile(gridX, gridY) == 'E') {
                    showAnnouncement("It ain't that easy", 500);
                    return;
                }

                // Use BFS to find the shortest path
                List<Point> path;
                try {
                    path = dungeon.bfs(player.getX(), player.getY(), gridX, gridY);
                } catch (InterruptedException | ExecutionException ex) {
                    if (ex.getCause() instanceof TimeoutException) {
                        // Handle TimeoutException
                        System.out.println("Pathfinding took too long and was cancelled.");
                        return;
                    }
                    throw new RuntimeException(ex);
                } catch (TimeoutException ex) {
                    throw new RuntimeException(ex);
                }

                // If path is null, don't start the timer
                if (path == null) {
                    return;
                }

                // Create a Timer to animate the movement
                Timer timer = new Timer(150, null); // 150ms delay between each move
                isTimerRunning = true; // Set the timer running flag
                List<Point> finalPath = path;
                timer.addActionListener(new ActionListener() {
                    int index = 0;

                    @Override
                    public void actionPerformed(ActionEvent event) {
                        if (index < finalPath.size()) {
                            // Clear the previous player position
                            dungeon.setTile(player.getX(), player.getY(), '.');

                            // Update the player's position
                            Point position = finalPath.get(index);
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
                timer.start(); // Start the timer
            }
        });

        pack(); // pack the frame first to calculate its preferred size

        // Set the size of the frame with additional padding for margins
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
            e.printStackTrace();
        }
    }

    // Initialize game state
    private void initializeGame() {
        startingDungeon.map[0][0] = 'P';
        player = new Player(0, 0);
        dungeon.setTile(player.getX(), player.getY(), 'P');
        showAnnouncement("Find Ciri to advance to next level", 1500);
    }

    public void generateNewLevel() {
        dungeon.setTile(player.getX(), player.getY(), '.'); // Clear the player's previous position
        generateMap();
        initializeGame();
    }

    // Generate map
    private void generateMap() {
        Random random = new Random();

        // Create first dungeon at a random position
        int firstDungeonX = random.nextInt(1, 2);
        int firstDungeonY = random.nextInt(1, 2);
        grid[firstDungeonX][firstDungeonY] = new Dungeon(WIDTH, HEIGHT, firstDungeonX, firstDungeonY);

        // Create the rest of the dungeons ensuring adjacency
        int ITERATE_TIMES = 2;
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int k = 0; k < ITERATE_TIMES; k++) {
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
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

        // Find the first non-null dungeon in the grid
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] != null) {
                    dungeon = grid[i][j];
                    startingDungeon = dungeon; // Store the starting dungeon
                }
            }
        }

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
        // Instead of calling super.paint(g), render directly to the buffer strategy
        do {
            do {
                Graphics bufferGraphics = bufferStrategy.getDrawGraphics();
                render(bufferGraphics);
                bufferGraphics.dispose();
            } while (bufferStrategy.contentsRestored());
            bufferStrategy.show();
        } while (bufferStrategy.contentsLost());
    }

    // Extract the rendering logic into a separate method
    private void render(Graphics g) {
        // Rendering logic goes here, similar to your existing paint method

        // Draw tiles
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

        // Draw message if present
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

        // Draw top area
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
