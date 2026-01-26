package io.cyborgcode.utilities.logging;


import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

/**
 * Centralized logging utility for managing Log4j2 loggers and markers.
 *
 * <p>This class provides methods for retrieving named loggers and registering log markers
 * that allow categorized and structured logging within the framework.
 * It ensures consistency in log management and prevents redundant marker creation.
 *
 * <p>Features:
 * <ul>
 *     <li>Provides named logger instances using {@link LogManager}.</li>
 *     <li>Manages custom markers via {@link MarkerManager} to categorize log messages.</li>
 *     <li>Utilizes an internal cache to avoid duplicate marker registrations.</li>
 * </ul>
 *
 * <p>This class is designed as a utility with static methods and cannot be instantiated.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public class LogCyborg {

   /**
    * A thread-safe map for storing and retrieving registered log markers.
    */
   private static final ConcurrentHashMap<String, Marker> MARKERS = new ConcurrentHashMap<>();


   private LogCyborg() {
   }

   /**
    * Registers and retrieves a named marker for logging purposes.
    *
    * <p>If a marker with the given name already exists in the cache, it is returned.
    * Otherwise, a new marker is created and stored.
    *
    * @param markerName The name of the marker to register.
    * @return The registered {@link Marker} instance.
    */
   public static Marker registerMarker(String markerName) {
      return MARKERS.computeIfAbsent(markerName, MarkerManager::getMarker);
   }

   /**
    * Retrieves a previously registered log marker.
    *
    * <p>If no marker with the given name exists, this method returns {@code null}.
    *
    * @param markerName The name of the marker.
    * @return The corresponding {@link Marker} instance, or {@code null} if not found.
    */
   public static Marker getMarker(String markerName) {
      return MARKERS.get(markerName);
   }

   /**
    * Retrieves a logger instance associated with a specific class.
    *
    * <p>The logger is retrieved using Log4j2's {@link LogManager}, ensuring that
    * logs are categorized under the given class's name.
    *
    * @param clazz The class whose logger is required.
    * @return A {@link Logger} instance corresponding to the specified class.
    */
   public static Logger getLogger(Class<?> clazz) {
      return LogManager.getLogger(clazz);
   }

   /**
    * Retrieves a logger instance by name.
    *
    * <p>This allows for dynamically named loggers, useful in scenarios where a specific
    * logging category needs to be established at runtime.
    *
    * @param name The name of the logger.
    * @return A {@link Logger} instance corresponding to the given name.
    */
   public static Logger getLogger(String name) {
      return LogManager.getLogger(name);
   }

}