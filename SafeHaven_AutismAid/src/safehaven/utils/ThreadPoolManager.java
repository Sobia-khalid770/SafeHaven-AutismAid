package safehaven.utils;

import java.util.concurrent.*;

/**
 * Manages a fixed-size thread pool for background tasks (audio, network, UI updates).
 * Prevents unbounded thread creation and resource leaks.
 * 
 * Usage:
 *   ThreadPoolManager.execute(() -> someLongOperation());
 * 
 * Shutdown gracefully on app exit:
 *   ThreadPoolManager.shutdown();
 */
public class ThreadPoolManager {
    private static ThreadPoolManager instance;
    private final ExecutorService executor;
    private static final int POOL_SIZE = 8;

    private ThreadPoolManager() {
        executor = Executors.newFixedThreadPool(POOL_SIZE, r -> {
            Thread t = new Thread(r, "SafeHaven-BG-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        });
        
        // Register shutdown hook for graceful cleanup on JVM exit
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("[ThreadPoolManager] Shutting down thread pool...");
            shutdown();
        }, "SafeHaven-Shutdown"));
    }

    public static synchronized ThreadPoolManager getInstance() {
        if (instance == null) instance = new ThreadPoolManager();
        return instance;
    }

    /**
     * Submit a task to the thread pool.
     */
    public static void execute(Runnable task) {
        getInstance().executor.execute(task);
    }

    /**
     * Submit a callable task and return a Future.
     */
    public static <T> Future<T> submit(Callable<T> task) {
        return getInstance().executor.submit(task);
    }

    /**
     * Graceful shutdown  wait 5 seconds for tasks to complete.
     */
    public static void shutdown() {
        ExecutorService es = getInstance().executor;
        es.shutdown();
        try {
            if (!es.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("[ThreadPoolManager] Tasks did not terminate; forcing shutdown.");
                es.shutdownNow();
            }
        } catch (InterruptedException e) {
            es.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Get the underlying ExecutorService (for advanced use cases).
     */
    public static ExecutorService getExecutor() {
        return getInstance().executor;
    }
}
