package game.object;

import game.Constants;
import game.object.entity.Exit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class GameMap {
    private final int DUNGEON_WIDTH, DUNGEON_HEIGHT, GRID_SIZE;
    private final Dungeon[][] grid;
    private Dungeon startingDungeon;

    public GameMap(int width, int height, int gridSize) {
        this.DUNGEON_WIDTH = width;
        this.DUNGEON_HEIGHT = height;
        this.GRID_SIZE = gridSize;

        this.grid = new Dungeon[GRID_SIZE][GRID_SIZE];
        generateMap();
    }

    private void generateMap() {
        Random random = new Random();

        createInitialDungeon(random);
        createMoreDungeons(random);
        addDoors();
        selectStartingDungeon();
        selectExitDungeon(random);
    }

    // Create the initial dungeon, from which all other dungeons branch out
    private void createInitialDungeon(Random random) {
        int firstDungeonX = random.nextInt(1, 2);
        int firstDungeonY = random.nextInt(1, 2);
        grid[firstDungeonX][firstDungeonY] = new Dungeon(DUNGEON_WIDTH, DUNGEON_HEIGHT, firstDungeonX, firstDungeonY);
    }

    private void createMoreDungeons(Random random) {
        AtomicInteger dungeonCount = new AtomicInteger(1); // Starting dungeon already placed

        // Determine the total number of dungeons to create (between 40% and 80% of the grid)
        int totalDungeons = GRID_SIZE * GRID_SIZE;
        int targetDungeonCount = random.nextInt((int) (totalDungeons * Constants.DUNGEON_TARGET_COUNT_LOW),
                (int) (totalDungeons * Constants.DUNGEON_TARGET_COUNT_HIGH));

        try (ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            for (int i = 0; i < GRID_SIZE; i += GRID_SIZE / 2) { // Divide the grid into chunks and submit tasks to the executor
                for (int j = 0; j < GRID_SIZE; j += GRID_SIZE / 2) {
                    final int chunkStartX = i;
                    final int chunkStartY = j;

                    // Submit a task to process each chunk
                    executor.submit(() -> IntStream.range(chunkStartX, Math.min(chunkStartX + GRID_SIZE / 2, GRID_SIZE)).forEach(x ->
                            IntStream.range(chunkStartY, Math.min(chunkStartY + GRID_SIZE / 2, GRID_SIZE)).forEach(y -> {
                                if (dungeonCount.get() >= targetDungeonCount) {
                                    return; // Stop creating dungeons if the target is reached
                                }
                                synchronized (grid) {
                                    if (grid[x][y] == null && hasAdjacentDungeon(x, y)) {
                                        boolean shouldCreateDungeon = random.nextDouble() < 0.67; // 2/3 chance
                                        if (shouldCreateDungeon && dungeonCount.get() < targetDungeonCount) {
                                            grid[x][y] = new Dungeon(DUNGEON_WIDTH, DUNGEON_HEIGHT, x, y);
                                            dungeonCount.incrementAndGet(); // Increment count safely
                                        }
                                    }
                                }
                            })
                    ));
                }
            }
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
