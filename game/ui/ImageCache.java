package game.ui;

import game.Constants;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageCache {
    private final Map<String, Image> cache;
    private static final Logger LOGGER = Logger.getLogger(ImageCache.class.getName());

    public ImageCache() {
        this.cache = new HashMap<>();
    }

    public void cacheImages(String characterImage) {
        ArrayList<String> imagePaths = new ArrayList<>();
        imagePaths.add(characterImage);
        imagePaths.addAll(Constants.OBJECT_IMAGE_MAP.values());

        try (ExecutorService executor = Executors.newFixedThreadPool(Constants.IMAGE_CACHE_THREAD_NUM)) {
            // Create a CompletableFuture for each image path
            List<CompletableFuture<Void>> futures = imagePaths.stream()
                    .map(path -> CompletableFuture.runAsync(() -> cacheImage(path), executor))
                    .toList();

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allFutures.join(); // Wait for all tasks to complete
        }
    }

    private void cacheImage(String path) {
        try {
            Image image = ImageIO.read(Objects.requireNonNull(getClass().getResource(path)));
            this.cache.put(path, image);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An exception occurred", e);
        }
    }

    public Image getImage(String path) {
        return this.cache.get(path);
    }

}
