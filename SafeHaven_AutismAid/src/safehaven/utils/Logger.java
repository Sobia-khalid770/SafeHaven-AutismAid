package safehaven.utils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple file-based logger for SafeHaven.
 * Logs to: System.err and optionally to file.
 * Usage: Logger.error("Login failed", e);
 */
public class Logger {
    private static Logger instance;
    private static final String LOG_FILE = System.getProperty("user.home") + "/.safehaven_debug.log";
    private PrintWriter fileWriter;

    private Logger() {
        try {
            fileWriter = new PrintWriter(new FileWriter(LOG_FILE, true), true);
        } catch (IOException e) {
            System.err.println("[Logger] Failed to open log file: " + e.getMessage());
        }
    }

    public static synchronized Logger getInstance() {
        if (instance == null) instance = new Logger();
        return instance;
    }

    private String timestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Log an error with optional exception.
     */
    public static void error(String msg) {
        getInstance().log("ERROR", msg, null);
    }

    public static void error(String msg, Exception e) {
        getInstance().log("ERROR", msg + " | " + e.getClass().getSimpleName() + ": " + e.getMessage(), e);
    }

    /**
     * Log an info message.
     */
    public static void info(String msg) {
        getInstance().log("INFO", msg, null);
    }

    /**
     * Log a warning.
     */
    public static void warn(String msg) {
        getInstance().log("WARN", msg, null);
    }

    private void log(String level, String msg, Exception e) {
        String line = String.format("[%s] %s: %s", timestamp(), level, msg);
        System.err.println(line);
        if (fileWriter != null) {
            fileWriter.println(line);
            if (e != null) {
                e.printStackTrace(fileWriter);
            }
            fileWriter.flush();
        }
    }

    /**
     * Close the log file (call on app shutdown).
     */
    public static void close() {
        if (getInstance().fileWriter != null) {
            getInstance().fileWriter.close();
        }
    }
}
