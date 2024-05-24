import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.Timer;

public class Game extends JFrame {
    // Variables to store game parameters
    private final String characterImage;
    private final int TILE_SIZE ;
    private final int WIDTH;
    private final int HEIGHT;
    private final int Y_OFFSET;

    // Game objects
    private Dungeon[][] grid;
    private Dungeon dungeon;
    private Player player;
    private BufferedImage offScreenBuffer;

    // Game state variables
    public int level = 1;
    private Map<String, Image> imageCache;
    private String message;
    private Timer messageTimer;

    public Game(String characterImage, int TILE_SIZE, int WIDTH, int HEIGHT, int Y_OFFSET) {
        // Initialize game parameters
        this.characterImage = characterImage;
        this.TILE_SIZE = TILE_SIZE;
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.Y_OFFSET = Y_OFFSET;

        //Game grid
        grid = new Dungeon[4][4];
        generateMap();

        // Preload images
        preloadImages();

        // Initialize game and display level announcement
        initializeGame();
        showLevelAnnouncement();

        // Set up JFrame properties
        setTitle("Dungeon Crawler");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Add key listener for player movement
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
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
                }

                // Update player position based on key input
                int newX = player.getX() + dx;
                int newY = player.getY() + dy;

                // Check for valid movement and update player position
                if (newX >= 0 && newX < WIDTH && newY >= 0 && newY < HEIGHT &&
                        (dungeon.getTile(newX, newY) == '.' || dungeon.getTile(newX, newY) == 'E')) {
                    dungeon.setTile(player.getX(), player.getY(), '.');
                    player.move(dx, dy);
                    dungeon.setTile(player.getX(), player.getY(), 'P');
                    repaint();
                }

                // Check for reaching the exit and advance to the next level
                int[] playerPos = player.getPosition();
                if (playerPos[0] == dungeon.exitX && playerPos[1] == dungeon.exitY) {
                    initializeGame();
                    level++;
                    showLevelAnnouncement();
                    repaint();
                }
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
    }

    // Preload images into image cache
    private void preloadImages() {
        imageCache = new HashMap<>();
        loadAndCacheImage(characterImage); // Load the provided character image
        loadAndCacheImage("/images/wall.png");
        loadAndCacheImage("/images/floor.png");
        loadAndCacheImage("/images/ciri.png");
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
        dungeon = new Dungeon(WIDTH, HEIGHT);
        player = new Player(0, 0);
        dungeon.setTile(player.getX(), player.getY(), 'P');
        dungeon.setTile(dungeon.exitX, dungeon.exitY, 'E');
    }

    // Generate map
    private void generateMap() {
        Random random = new Random();

        // Create first dungeon at a random position
        int firstDungeonX = random.nextInt(1, 2);
        int firstDungeonY = random.nextInt(1, 2);
        grid[firstDungeonX][firstDungeonY] = new Dungeon(WIDTH, HEIGHT);

        // Create the rest of the dungeons ensuring adjacency
        for (int k = 0; k < 2; k++) {
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (grid[i][j] == null && hasAdjacentDungeon(i, j)) {
                        // Randomly decide whether to create a dungeon
                        boolean shouldCreateDungeon = random.nextBoolean();
                        if (shouldCreateDungeon) {
                            grid[i][j] = new Dungeon(WIDTH, HEIGHT);
                        }
                    }
                }
            }
        }

        // Print the generated map
        for (int i = 0; i < 4; i++) {
            System.out.println();
            for (int j = 0; j < 4; j++) {
                if (grid[i][j] != null) {
                    System.out.print("D");
                } else {
                    System.out.print(".");
                }
            }
        }
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
        if (y < 3 && grid[x][y + 1] != null) { // Check down
            return true;
        }
        return false;
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (offScreenBuffer == null || offScreenBuffer.getWidth() != getWidth() || offScreenBuffer.getHeight() != getHeight()) {
            offScreenBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

        Graphics bufferGraphics = offScreenBuffer.getGraphics();

        // Draw top area
        bufferGraphics.setColor(new Color(50, 50, 50));
        bufferGraphics.fillRect(0, 0, getWidth(), Y_OFFSET);

        // Draw level and character name
        bufferGraphics.setColor(Color.WHITE);
        bufferGraphics.setFont(new Font("Times", Font.BOLD, 20));
        bufferGraphics.drawString("Level: " + level, 15, Y_OFFSET - 16);
        if (Objects.equals(characterImage, "/images/geralt.png")) {
            bufferGraphics.drawString("Geralt", 825, Y_OFFSET - 16);
        }
        else if (Objects.equals(characterImage, "/images/yen.png")) {
            bufferGraphics.drawString("Yennefer", 800, Y_OFFSET - 16);
        }

        // Draw tiles
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                char tile = dungeon.getTile(x, y);
                Image imageToDraw = switch (tile) {
                    case '#' -> getImageFromCache("/images/wall.png");
                    case '.' -> getImageFromCache("/images/floor.png");
                    case 'P' -> getImageFromCache(characterImage);
                    case 'E' -> getImageFromCache("/images/ciri.png");
                    default -> null;
                };
                if (imageToDraw != null) {
                    bufferGraphics.drawImage(imageToDraw, x * TILE_SIZE, Y_OFFSET - 8 + y * TILE_SIZE, TILE_SIZE, TILE_SIZE, this);
                }
            }
        }

        g.drawImage(offScreenBuffer, 0, 0, this);

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
    }

    // Retrieve image from cache
    private Image getImageFromCache(String path) {
        return imageCache.get(path);
    }

    // Display level announcement message
    private void showLevelAnnouncement() {
        message = "Welcome to Level " + level;
        if (messageTimer != null) {
            messageTimer.stop();
        }
        messageTimer = new Timer(1000, e -> {
            message = null;
            repaint();
        });
        messageTimer.setRepeats(false);
        messageTimer.start();
        repaint();
    }
}
