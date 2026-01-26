package io.cyborgcode.utilities.logging;

/**
 * Centralized logging utility for common framework logs.
 *
 * <p>This class provides structured logging methods for different log levels, including
 * informational, warning, error, debug, and step-level logs. It follows a singleton
 * pattern to maintain a consistent logging instance across the framework.
 *
 * <p>Features:
 * <ul>
 *     <li>Singleton instance ensuring uniform logging across the framework.</li>
 *     <li>Encapsulates various log levels, including step and extended logging.</li>
 *     <li>Provides a mechanism to override the singleton instance for customization.</li>
 * </ul>
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public final class LogCommon extends LogCore {

   /**
    * Singleton instance of {@code LogCommon}.
    */
   private static LogCommon instance;

   /**
    * Private constructor to enforce singleton pattern.
    */
   private LogCommon() {
      super("Cyborg.COMMON", "COMMON");
   }

   /**
    * Logs an informational message.
    *
    * @param message The message to log.
    * @param args    Optional arguments for message formatting.
    */
   public static void info(String message, Object... args) {
      getInstance().infoLog(message, args);
   }

   /**
    * Logs a warning message.
    *
    * @param message The warning message to log.
    * @param args    Optional arguments for message formatting.
    */
   public static void warn(String message, Object... args) {
      getInstance().warnLog(message, args);
   }

   /**
    * Logs an error message.
    *
    * @param message The error message to log.
    * @param args    Optional arguments for message formatting.
    */
   public static void error(String message, Object... args) {
      getInstance().errorLog(message, args);
   }

   /**
    * Logs a debug-level message.
    *
    * @param message The debug message to log.
    * @param args    Optional arguments for message formatting.
    */
   public static void debug(String message, Object... args) {
      getInstance().debugLog(message, args);
   }

   /**
    * Logs a trace-level message.
    *
    * @param message The trace message to log.
    * @param args    Optional arguments for message formatting.
    */
   public static void trace(String message, Object... args) {
      getInstance().traceLog(message, args);
   }

   /**
    * Logs a step-level message, indicating progress in execution.
    *
    * @param message The step message to log.
    * @param args    Optional arguments for message formatting.
    */
   public static void step(String message, Object... args) {
      getInstance().stepLog(message, args);
   }

   /**
    * Logs an extended-level message if extended logging is enabled.
    *
    * @param message The extended log message.
    * @param args    Optional arguments for message formatting.
    */
   public static void extended(String message, Object... args) {
      getInstance().extendedLog(message, args);
   }

   /**
    * Replaces the current singleton instance with a new {@code LogCommon} instance.
    *
    * @param instance The new instance of {@code LogCommon} to use.
    * @param <T>      A type extending {@code LogCore}.
    */
   public static <T extends LogCore> void extend(final T instance) {
      LogCommon.instance = (LogCommon) instance;
   }

   /**
    * Retrieves the singleton instance of {@code LogCommon}.
    *
    * <p>If no instance exists, a new one is created.
    *
    * @return The {@code LogCommon} singleton instance.
    */
   private static LogCommon getInstance() {
      if (instance == null) {
         instance = new LogCommon();
      }
      return instance;
   }

}
