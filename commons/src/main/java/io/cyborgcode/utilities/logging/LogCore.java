package io.cyborgcode.utilities.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;

/**
 * Provides a core logging mechanism for structured and categorized logging.
 *
 * <p>This abstract class serves as a base for logging operations, utilizing Log4j2
 * for different logging levels, including standard logs (info, warn, error, debug, trace)
 * and custom levels such as {@code STEP}, {@code VALIDATION}, and {@code EXTENDED}.
 * It integrates with {@link LogCyborg} to manage logger instances and markers.
 *
 * <p>Extended logging can be controlled via the system property {@code extended.logging}.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public abstract class LogCore {

   /**
    * The Log4j2 logger instance used for logging messages.
    */
   private final Logger logger;

   /**
    * The marker associated with log messages for categorization.
    */
   private final Marker marker;

   private static Boolean silentMode;

   /**
    * Custom log level for step-based logs.
    */
   private static final Level STEP_LEVEL = Level.forName("STEP", 350);

   /**
    * Custom log level for validation-related logs.
    */
   private static final Level VALIDATION_LEVEL = Level.forName("VALIDATION", 350);

   /**
    * Custom log level for extended logs.
    */
   private static final Level EXTENDED_LEVEL = Level.forName("EXTENDED", 450);


   /**
    * Flag indicating whether extended logging is enabled, controlled via system properties.
    */
   private static Boolean extendedLogging;


   /**
    * Initializes the logger and marker for a specific logging category.
    *
    * @param loggerName The name of the logger.
    * @param markerName The name of the marker associated with this logger.
    */
   protected LogCore(String loggerName, String markerName) {
      this.logger = LogCyborg.getLogger(loggerName);
      this.marker = LogCyborg.registerMarker(markerName);
   }


   /**
    * Logs an informational message.
    *
    * @param message The log message.
    * @param args    Arguments to be formatted within the message.
    */
   protected void infoLog(String message, Object... args) {
      logger.info(marker, message, args);
   }


   /**
    * Logs a warning message unless silent mode is enabled.
    *
    * @param message The log message.
    * @param args    Arguments to be formatted within the message.
    */
   protected void warnLog(String message, Object... args) {
      if (!isSilent()) {
         logger.warn(marker, message, args);
      }
   }


   /**
    * Logs an error message unless silent mode is enabled.
    *
    * @param message The log message.
    * @param args    Arguments to be formatted within the message.
    */
   protected void errorLog(String message, Object... args) {
      if (!isSilent()) {
         logger.error(marker, message, args);
      }
   }


   /**
    * Logs a debug message unless silent mode is enabled.
    *
    * @param message The log message.
    * @param args    Arguments to be formatted within the message.
    */
   protected void debugLog(String message, Object... args) {
      if (!isSilent()) {
         logger.debug(marker, message, args);
      }
   }


   /**
    * Logs a trace message unless silent mode is enabled.
    *
    * @param message The log message.
    * @param args    Arguments to be formatted within the message.
    */
   protected void traceLog(String message, Object... args) {
      if (!isSilent()) {
         logger.trace(marker, message, args);
      }
   }


   /**
    * Logs a step-based message unless silent mode is enabled.
    *
    * <p>Step-based logs are commonly used for tracking test execution steps in automation frameworks.
    *
    * @param message The log message.
    * @param args    Arguments to be formatted within the message.
    */
   protected void stepLog(String message, Object... args) {
      if (!isSilent()) {
         logger.log(STEP_LEVEL, marker, message, args);
      }
   }


   /**
    * Logs a validation-related message unless silent mode is enabled.
    *
    * <p>This log level is used to indicate validation results, ensuring test assertions
    * or expected conditions are met.
    *
    * @param message The log message.
    * @param args    Arguments to be formatted within the message.
    */
   protected void validationLog(String message, Object... args) {
      if (!isSilent()) {
         logger.log(VALIDATION_LEVEL, marker, message, args);
      }
   }


   /**
    * Logs an extended message unless silent mode is enabled.
    *
    * <p>Extended logs provide additional details and are useful for debugging complex behaviors.
    * This log level can be enabled/disabled via the system property {@code extended.logging}.
    *
    * @param message The log message.
    * @param args    Arguments to be formatted within the message.
    */
   protected void extendedLog(String message, Object... args) {
      if (!isSilent() && extendedLoggingEnabled()) {
         logger.log(EXTENDED_LEVEL, marker, message, args);
      }
   }


   /**
    * Determines whether extended logging is enabled based on the system property {@code extended.logging}.
    *
    * @return {@code true} if extended logging is enabled, otherwise {@code false}.
    */
   private static boolean extendedLoggingEnabled() {
      if (extendedLogging == null) {
         extendedLogging = Boolean.parseBoolean(System.getProperty("extended.logging", "false"));
      }
      return extendedLogging;
   }


   /**
    * Determines whether the application is running in silent mode based on the system property {@code silent.mode}.
    *
    * <p>When silent mode is enabled (i.e., {@code silent.mode=true}), all non-informational logs will be suppressed.
    * This includes warning, error, debug, trace, step, validation, and extended logs.
    *
    * <p>By default, silent mode is disabled ({@code false}).
    *
    * @return {@code true} if silent mode is enabled, otherwise {@code false}.
    */
   private static boolean isSilent() {
      if (silentMode == null) {
         silentMode = Boolean.parseBoolean(System.getProperty("silent.mode", "false"));
      }
      return silentMode;
   }


}