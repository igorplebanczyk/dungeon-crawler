package game;

import game.objects.Exit;
import game.objects.Tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class GameMap {
    private final int DUNGEON_WIDTH, DUNGEON_HEIGHT, GRID_SIZE;
    private final Dungeon[][] grid;
    private Dungeon startingDungeon;

    private static final Logger LOGGER = Logger.getLogger(GameMap.class.getName());

    public GameMap(int width, int height, int gridSize) {
        this.DUNGEON_WIDTH = width;
        this.DUNGEON_HEIGHT = height;
        this.GRID_SIZE = gridSize;

        this.grid = new Dungeon[GRID_SIZE][GRID_SIZE];
        generateMap();
    }

    private void generateMap() {
        Random random = new Random();

        createDungeons(random);
        addDoors();
        selectStartingDungeon();
        selectExitDungeon(random);
    }

    // Create dungeons on the grid
    private void createDungeons(Random random) {
        createInitialDungeon(random);

        // Create the rest of the dungeons ensuring adjacency, using multithreading
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

    // Create the initial dungeon, from which all other dungeons branch out
    private void createInitialDungeon(Random random) {
        int firstDungeonX = random.nextInt(1, 2);
        int firstDungeonY = random.nextInt(1, 2);
        grid[firstDungeonX][firstDungeonY] = new Dungeon(DUNGEON_WIDTH, DUNGEON_HEIGHT, firstDungeonX, firstDungeonY);
    }

    // Populate the grid with more dungeons
    private void createMoreDungeons(Random random) {
        AtomicInteger dungeonCount = new AtomicInteger(1); // Starting dungeon already placed, use atomic because it allows the variable to mutated inside a lambda
        int targetDungeonCount = (int) (GRID_SIZE * GRID_SIZE * 0.5); // 50% of the grid
        while (dungeonCount.get() < targetDungeonCount) {
            IntStream.range(0, GRID_SIZE).forEach(i -> IntStream.range(0, GRID_SIZE).forEach(j -> {
                if (grid[i][j] == null && hasAdjacentDungeon(i, j)) {
                    boolean shouldCreateDungeon = random.nextDouble() < 0.67; // 2/3 chance
                    if (shouldCreateDungeon) {
                        grid[i][j] = new Dungeon(DUNGEON_WIDTH, DUNGEON_HEIGHT, i, j);
                        dungeonCount.incrementAndGet(); // Increment the count safely
                    }
                }
            }));
        }
    }

    private void addDoors() {
        // Add doors
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                Dungeon d = grid[i][j];
                if (d != null) {
                    if (i > 0 && grid[i - 1][j] != null) { // Check left
                        d.addDoor(0, DUNGEON_HEIGHT / 2);
                    }
                    if (j > 0 && grid[i][j - 1] != null) { // Check up
                        d.addDoor(DUNGEON_WIDTH / 2, 0);
                    }
                    if (i < GRID_SIZE - 1 && grid[i + 1][j] != null) { // Check right
                        d.addDoor(DUNGEON_WIDTH - 1, DUNGEON_HEIGHT / 2);
                    }
                    if (j < GRID_SIZE - 1 && grid[i][j + 1] != null) { // Check down
                        d.addDoor(DUNGEON_WIDTH / 2, DUNGEON_HEIGHT - 1);
                    }
                }
            }
        }
    }

    private void selectStartingDungeon() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] != null) {
                    startingDungeon = grid[i][j];
                }
            }
        }
    }

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
        exitDungeon.setTile(exitDungeon.getExitX(), exitDungeon.getExitY(), new Exit()); // Set the exit in the selected dungeon
    }

    private boolean hasAdjacentDungeon(int x, int y) {
        if (x > 0 && grid[x - 1][y] != null) { // Check left
            return true;
        }
        if (y > 0 && grid[x][y - 1] != null) { // Check up
            return true;
        }
        if (x < GRID_SIZE - 1 && grid[x + 1][y] != null) { // Check right
            return true;
        }
        return y < GRID_SIZE - 1 && grid[x][y + 1] != null; // Check down
    }

    public Dungeon getStartingDungeon() {
        return startingDungeon;
    }

    public Dungeon[][] getGrid() {
        return grid;
    }
}
