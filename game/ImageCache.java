package game;

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

    // Preload images into image cache
    public void cacheImages(String characterImage) {
        ArrayList<String> imagePaths = new ArrayList<>();
        imagePaths.add(characterImage);
        imagePaths.addAll(Constants.OBJECT_IMAGE_MAP.values());

        ExecutorService executor = Executors.newFixedThreadPool(Constants.IMAGE_CACHE_THREAD_NUM);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String path : imagePaths) {
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                cacheImage(path);
                return null;
                }, executor);
            futures.add(future);
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        executor.shutdown();
    }

    private void cacheImage(String path) {
        try {
            Image image = ImageIO.read(Objects.requireNonNull(getClass().getResource(path)));
            this.cache.put(path, image);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "An exception occurred", e);
        }
    }

    // Retrieve image from cache
    public Image getImage(String path) {
        return this.cache.get(path);
    }

}
