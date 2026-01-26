package io.cyborgcode.utilities.logging;

import io.cyborgcode.utilities.logging.mock.DummyLogCore;
import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@DisplayName("LogCore Tests")
class LogCoreTest {

   private static final String INFO = "info";
   private static final String WARN = "warn";
   private static final String ERROR = "error";
   private static final String DEBUG = "debug";
   private static final String TRACE = "trace";
   private static final String STEP = "step";
   private static final String VALIDATION = "validation";
   private static final String EXTENDED = "extended";
   private static final String ARG_1 = "arg1";
   private static final String ARG_2 = "arg2";
   private static final String TEST_LOGGER = "TestLogger";
   private static final String TEST_MARKER = "TestMarker";
   private static final String EXTENDED_MESSAGE = "Extended message";
   private static final String EXTENDED_LOGGING_FIELD = "extendedLogging";
   private static final String SILENT_MODE = "silentMode";


   private Logger mockLogger;
   private Marker mockMarker;
   private DummyLogCore dummyLogCore;
   private MockedStatic<LogCyborg> logCyborgMockedStatic;


   @BeforeEach
   void setup() {
      // Setup mocks
      mockLogger = mock(Logger.class);
      mockMarker = mock(Marker.class);

      // Mock static methods
      logCyborgMockedStatic = mockStatic(LogCyborg.class);
      logCyborgMockedStatic.when(() -> LogCyborg.getLogger(TEST_LOGGER)).thenReturn(mockLogger);
      logCyborgMockedStatic.when(() -> LogCyborg.registerMarker(TEST_MARKER)).thenReturn(mockMarker);

      // Create the test instance
      dummyLogCore = new DummyLogCore(TEST_LOGGER, TEST_MARKER);

   }


   @AfterEach
   void tearDown() {
      logCyborgMockedStatic.close();
   }


   @Nested
   @DisplayName("Standard Logging Methods Tests")
   class StandardLoggingMethodsTests {

      @ParameterizedTest(name = "Test {1} logging")
      @MethodSource("io.cyborgcode.utilities.logging.LogCoreTest#loggingMethods")
      @DisplayName("Should log messages at appropriate levels")
      void testLoggingMethods(BiConsumer<DummyLogCore, String[]> method, String methodName) {
         // Given
         String message = "Test %s message".formatted(methodName);
         String[] args = new String[] {message, ARG_1, ARG_2};

         // When
         method.accept(dummyLogCore, args);

         // Then - Verify correct logger method was called
         verifyLoggerCall(methodName, message);
      }


      private void verifyLoggerCall(String methodName, String message) {
         switch (methodName) {
            case INFO -> verify(mockLogger).info(eq(mockMarker), eq(message),
                  aryEq(new Object[] {ARG_1, ARG_2}));
            case WARN -> verify(mockLogger).warn(eq(mockMarker), eq(message),
                  aryEq(new Object[] {ARG_1, ARG_2}));
            case ERROR -> verify(mockLogger).error(eq(mockMarker), eq(message),
                  aryEq(new Object[] {ARG_1, ARG_2}));
            case DEBUG -> verify(mockLogger).debug(eq(mockMarker), eq(message),
                  aryEq(new Object[] {ARG_1, ARG_2}));
            case TRACE -> verify(mockLogger).trace(eq(mockMarker), eq(message),
                  aryEq(new Object[] {ARG_1, ARG_2}));
            case STEP -> {
               ArgumentCaptor<Level> levelCaptor = ArgumentCaptor.forClass(Level.class);
               verify(mockLogger).log(levelCaptor.capture(), eq(mockMarker), eq(message),
                     aryEq(new Object[] {ARG_1, ARG_2}));
               Level capturedLevel = levelCaptor.getValue();
               assertNotNull(capturedLevel, "Level should not be null");
               assertEquals("STEP", capturedLevel.name(), "Level name should be STEP");
               assertEquals(350, capturedLevel.intLevel(), "Level value should be 350");
            }
            case VALIDATION -> {
               ArgumentCaptor<Level> levelCaptor = ArgumentCaptor.forClass(Level.class);
               verify(mockLogger).log(levelCaptor.capture(), eq(mockMarker), eq(message),
                     aryEq(new Object[] {ARG_1, ARG_2}));
               Level capturedLevel = levelCaptor.getValue();
               assertNotNull(capturedLevel, "Level should not be null");
               assertEquals("VALIDATION", capturedLevel.name(), "Level name should be VALIDATION");
               assertEquals(350, capturedLevel.intLevel(), "Level value should be 350");
            }
            case EXTENDED -> {
               ArgumentCaptor<Level> levelCaptor = ArgumentCaptor.forClass(Level.class);
               verify(mockLogger).log(levelCaptor.capture(), eq(mockMarker), eq(message),
                     aryEq(new Object[] {ARG_1, ARG_2}));
               Level capturedLevel = levelCaptor.getValue();
               assertNotNull(capturedLevel, "Level should not be null");
               assertEquals("EXTENDED", capturedLevel.name(), "Level name should be EXTENDED");
               assertEquals(350, capturedLevel.intLevel(), "Level value should be 350");
            }
            default -> throw new IllegalArgumentException("Unexpected method: " + methodName);
         }
      }

   }

   @Nested
   @DisplayName("Extended Logging Tests")
   class ExtendedLoggingTests {

      @Test
      @DisplayName("Should log extended messages when enabled")
      void testExtendedLogWhenEnabled() throws Exception {
         // Given
         setExtendedLogging(true);

         // When
         dummyLogCore.extendedLog(EXTENDED_MESSAGE, ARG_1, ARG_2);

         // Then
         ArgumentCaptor<Level> levelCaptor = ArgumentCaptor.forClass(Level.class);
         verify(mockLogger).log(
               levelCaptor.capture(),
               eq(mockMarker),
               eq(EXTENDED_MESSAGE),
               aryEq(new Object[] {ARG_1, ARG_2})
         );

         Level capturedLevel = levelCaptor.getValue();
         assertNotNull(capturedLevel, "Level should not be null");
         assertEquals("EXTENDED", capturedLevel.name(), "Level name should be EXTENDED");
      }


      @Test
      @DisplayName("Should not log extended messages when disabled")
      void testExtendedLogWhenDisabled() throws Exception {
         // Given
         setExtendedLogging(false);

         // When
         dummyLogCore.extendedLog(EXTENDED_MESSAGE, ARG_1, ARG_2);

         // Then
         verify(mockLogger, never()).log(any(Level.class), eq(mockMarker), anyString(), any(Object[].class));
      }

   }

   @Nested
   @DisplayName("System Property Tests")
   class SystemPropertyTests {

      @Test
      @DisplayName("Should read extended logging property from system")
      void testExtendedLoggingSystemProperty() throws Exception {
         // Given - Clear cached value
         Field field = LogCore.class.getDeclaredField(EXTENDED_LOGGING_FIELD);
         field.setAccessible(true);
         field.set(null, null);

         // Set system property
         System.setProperty("extended.logging", "true");

         try {
            // When
            dummyLogCore.extendedLog(EXTENDED_MESSAGE, ARG_1, ARG_2);

            // Then
            verify(mockLogger).log(
                  any(Level.class),
                  eq(mockMarker),
                  eq(EXTENDED_MESSAGE),
                  aryEq(new Object[] {ARG_1, ARG_2})
            );
         } finally {
            // Clean up
            System.clearProperty("extended.logging");
            field.set(null, null); // Reset for other tests
         }
      }


      @Test
      @DisplayName("Should default to false when system property not set")
      void testExtendedLoggingDefaultValue() throws Exception {
         // Given - Clear cached value and ensure property is not set
         Field field = LogCore.class.getDeclaredField(EXTENDED_LOGGING_FIELD);
         field.setAccessible(true);
         field.set(null, null);
         System.clearProperty("extended.logging");

         // When
         dummyLogCore.extendedLog(EXTENDED_MESSAGE, ARG_1, ARG_2);

         // Then
         verify(mockLogger, never()).log(
               any(Level.class),
               eq(mockMarker),
               anyString(),
               any(Object[].class)
         );
      }

   }

   @Nested
   @DisplayName("Silent Mode Tests")
   class SilentModeTests {

      private static final String SILENT_MODE_PROPERTY = "silent.mode";
      private static final String EXTENDED_LOGGING_PROPERTY = "extended.logging";


      @BeforeEach
      void clearSystemPropertiesBefore() throws Exception {
         setSilentMode(false);
         System.clearProperty(EXTENDED_LOGGING_PROPERTY);
         resetExtendedLoggingCache();
      }


      @AfterEach
      void clearSystemPropertiesAfter() throws Exception {
         setSilentMode(false);
         System.clearProperty(EXTENDED_LOGGING_PROPERTY);
         resetExtendedLoggingCache();
      }


      @Test
      @DisplayName("Should suppress all logs when silent mode is enabled")
      void shouldSuppressLogsWhenSilentModeEnabled() throws Exception {
         // -- Arrange --
         setSilentMode(true);
         setExtendedLogging(true); // Extended also needs to be true for test

         // -- Act --
         dummyLogCore.warnLog("Warn message", ARG_1);
         dummyLogCore.errorLog("Error message", ARG_1);
         dummyLogCore.debugLog("Debug message", ARG_1);
         dummyLogCore.traceLog("Trace message", ARG_1);
         dummyLogCore.stepLog("Step message", ARG_1);
         dummyLogCore.validationLog("Validation message", ARG_1);
         dummyLogCore.extendedLog("Extended message", ARG_1);

         // -- Assert --
         verifyNoInteractions(mockLogger);
      }


      @Test
      @DisplayName("Should allow all logs when silent mode is disabled")
      void shouldAllowLogsWhenSilentModeDisabled() throws Exception {
         // -- Arrange --
         setSilentMode(false);
         setExtendedLogging(true); // Enable extended logging


         // -- Act --
         dummyLogCore.warnLog("Warn message", ARG_1);
         dummyLogCore.errorLog("Error message", ARG_1);
         dummyLogCore.debugLog("Debug message", ARG_1);
         dummyLogCore.traceLog("Trace message", ARG_1);
         dummyLogCore.stepLog("Step message", ARG_1);
         dummyLogCore.validationLog("Validation message", ARG_1);
         dummyLogCore.extendedLog("Extended message", ARG_1);

         // -- Assert --
         verify(mockLogger).warn(any(Marker.class), eq("Warn message"), aryEq(new Object[] {ARG_1}));
         verify(mockLogger).error(any(Marker.class), eq("Error message"), aryEq(new Object[] {ARG_1}));
         verify(mockLogger).debug(any(Marker.class), eq("Debug message"), aryEq(new Object[] {ARG_1}));
         verify(mockLogger).trace(any(Marker.class), eq("Trace message"), aryEq(new Object[] {ARG_1}));
         verify(mockLogger).log(eq(Level.forName("STEP", 350)), any(Marker.class), eq("Step message"),
               aryEq(new Object[] {ARG_1}));
         verify(mockLogger).log(eq(Level.forName("VALIDATION", 350)), any(Marker.class), eq("Validation message"),
               aryEq(new Object[] {ARG_1}));
         verify(mockLogger).log(eq(Level.forName("EXTENDED", 450)), any(Marker.class), eq("Extended message"),
               aryEq(new Object[] {ARG_1}));
      }


      private void resetExtendedLoggingCache() throws Exception {
         Field field = LogCore.class.getDeclaredField("extendedLogging");
         field.setAccessible(true);
         field.set(null, null);
      }

   }


   private void setExtendedLogging(boolean value) throws Exception {
      Field field = LogCore.class.getDeclaredField(EXTENDED_LOGGING_FIELD);
      field.setAccessible(true);
      field.set(null, value);
   }


   private void setSilentMode(boolean value) throws Exception {
      Field field = LogCore.class.getDeclaredField(SILENT_MODE);
      field.setAccessible(true);
      field.set(null, value);
   }


   static Stream<Arguments> loggingMethods() {
      return Stream.of(
            Arguments.of((BiConsumer<DummyLogCore, String[]>) (core, a) -> core.infoLog(a[0], a[1], a[2]), INFO),
            Arguments.of((BiConsumer<DummyLogCore, String[]>) (core, a) -> core.warnLog(a[0], a[1], a[2]), WARN),
            Arguments.of((BiConsumer<DummyLogCore, String[]>) (core, a) -> core.errorLog(a[0], a[1], a[2]), ERROR),
            Arguments.of((BiConsumer<DummyLogCore, String[]>) (core, a) -> core.debugLog(a[0], a[1], a[2]), DEBUG),
            Arguments.of((BiConsumer<DummyLogCore, String[]>) (core, a) -> core.traceLog(a[0], a[1], a[2]), TRACE),
            Arguments.of((BiConsumer<DummyLogCore, String[]>) (core, a) -> core.stepLog(a[0], a[1], a[2]), STEP),
            Arguments.of((BiConsumer<DummyLogCore, String[]>) (core, a) -> core.validationLog(a[0], a[1], a[2]),
                  VALIDATION)
      );
   }

}