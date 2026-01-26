package io.cyborgcode.utilities.logging;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("LogCyborg Tests")
class LogCyborgTest {

   private static final String MARKERS_FIELD = "MARKERS";
   private static final String TEST_MARKER = "TEST_MARKER";
   private static final String EXISTING_MARKER = "EXISTING_MARKER";
   private static final String NON_EXISTENT_MARKER = "NON_EXISTENT_MARKER";
   private static final String TEST_LOGGER = "TestLogger";

   private ConcurrentHashMap<String, Marker> markers;

   @BeforeEach
   void setUp() throws Exception {
      Field field = LogCyborg.class.getDeclaredField(MARKERS_FIELD);
      field.setAccessible(true);
      markers = (ConcurrentHashMap<String, Marker>) field.get(null);
      markers.clear();
   }

   @Nested
   @DisplayName("Marker Registration Tests")
   class MarkerRegistrationTests {
      @Test
      @DisplayName("Should create new marker when it doesn't exist")
      void registerMarker_shouldCreateNewMarker_whenNotExists() {
         // When
         Marker marker = LogCyborg.registerMarker(TEST_MARKER);

         // Then
         assertAll(
               () -> assertNotNull(marker, "Marker should not be null"),
               () -> assertEquals(TEST_MARKER, marker.getName(), "Marker name should match"),
               () -> assertTrue(markers.containsKey(TEST_MARKER), "Marker should be stored in the map")
         );
      }

      @Test
      @DisplayName("Should return existing marker when already registered")
      void registerMarker_shouldReturnExistingMarker_whenAlreadyExists() {
         // Given
         Marker existingMarker = MarkerManager.getMarker(EXISTING_MARKER);
         markers.put(EXISTING_MARKER, existingMarker);

         // When
         Marker result = LogCyborg.registerMarker(EXISTING_MARKER);

         // Then
         assertSame(existingMarker, result, "Should return the same marker instance");
      }
   }

   @Nested
   @DisplayName("Marker Retrieval Tests")
   class MarkerRetrievalTests {
      @Test
      @DisplayName("Should return existing marker")
      void getMarker_shouldReturnExistingMarker() {
         // Given
         Marker expected = MarkerManager.getMarker(EXISTING_MARKER);
         markers.put(EXISTING_MARKER, expected);

         // When
         Marker result = LogCyborg.getMarker(EXISTING_MARKER);

         // Then
         assertSame(expected, result, "Should return the registered marker");
      }

      @Test
      @DisplayName("Should return null for non-existent marker")
      void getMarker_shouldReturnNullForNonExistentMarker() {
         // When
         Marker result = LogCyborg.getMarker(NON_EXISTENT_MARKER);

         // Then
         assertNull(result, "Should return null for unregistered marker");
      }
   }

   @Nested
   @DisplayName("Logger Retrieval Tests")
   class LoggerRetrievalTests {
      @Test
      @DisplayName("Should return logger with class name")
      void getLogger_shouldReturnLoggerWithClassName() {
         // When
         Logger logger = LogCyborg.getLogger(LogCyborgTest.class);

         // Then
         assertEquals(LogCyborgTest.class.getName(), logger.getName(),
               "Logger name should match class name");
      }

      @Test
      @DisplayName("Should return logger with specified name")
      void getLogger_shouldReturnLoggerWithSpecifiedName() {
         // When
         Logger logger = LogCyborg.getLogger(TEST_LOGGER);

         // Then
         assertEquals(TEST_LOGGER, logger.getName(),
               "Logger name should match specified name");
      }
   }
}