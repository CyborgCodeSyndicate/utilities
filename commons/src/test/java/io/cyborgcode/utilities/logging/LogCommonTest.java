package io.cyborgcode.utilities.logging;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("LogCommon Tests")
class LogCommonTest {

   private static final String ARG_1 = "arg1";
   private static final String ARG_2 = "arg2";
   private static final String INFO = "info";
   private static final String WARN = "warn";
   private static final String ERROR = "error";
   private static final String DEBUG = "debug";
   private static final String TRACE = "trace";
   private static final String STEP = "step";
   private static final String EXTENDED = "extended";
   private static final String INSTANCE_FIELD = "instance";

   @Nested
   @DisplayName("Static Logging Methods Tests")
   class StaticLoggingMethodsTests {
      @ParameterizedTest(name = "Test {1} logging")
      @MethodSource("io.cyborgcode.utilities.logging.LogCommonTest#loggingMethods")
      @DisplayName("Should delegate logging to instance methods")
      void testLoggingMethods(BiConsumer<String, Object[]> logMethod, String methodName) {
         // Given
         Object[] args = new Object[] {ARG_1, ARG_2};
         String message = formatMessage(methodName);
         LogCommon mockInstance = mock(LogCommon.class);

         // When
         LogCommon.extend(mockInstance);
         logMethod.accept(message, args);

         // Then
         verifyLogMethod(mockInstance, methodName, message, args);
      }
   }

   @Nested
   @DisplayName("Instance Management Tests")
   class InstanceManagementTests {
      @Test
      @DisplayName("Should allow extending with custom instance")
      void testExtend() throws Exception {
         // Given
         Field instanceField = getInstanceField();
         Object originalInstance = instanceField.get(null);
         LogCommon mockInstance = mock(LogCommon.class);

         // When
         LogCommon.extend(mockInstance);

         // Then
         assertSame(mockInstance, instanceField.get(null),
               "Instance field should be set to the mock");

         // Restore original instance
         instanceField.set(null, originalInstance);
      }

      @Test
      @DisplayName("Should create and reuse singleton instance")
      void testSingletonInitialization() throws Exception {
         // Given - Clear existing instance
         Field instanceField = getInstanceField();
         Object originalInstance = instanceField.get(null);
         instanceField.set(null, null);

         Method getInstanceMethod = LogCommon.class.getDeclaredMethod("getInstance");
         getInstanceMethod.setAccessible(true);

         // When - Create first instance
         Object firstInstance = getInstanceMethod.invoke(null);

         // Then
         assertNotNull(firstInstance, "First instance should not be null");

         // When - Create second instance
         Object secondInstance = getInstanceMethod.invoke(null);

         // Then
         assertSame(firstInstance, secondInstance,
               "Second call should return the same instance (singleton)");

         // Restore original instance
         instanceField.set(null, originalInstance);
      }
   }

   private static Stream<Arguments> loggingMethods() {
      return Stream.of(
            Arguments.of((BiConsumer<String, Object[]>) LogCommon::info, INFO),
            Arguments.of((BiConsumer<String, Object[]>) LogCommon::warn, WARN),
            Arguments.of((BiConsumer<String, Object[]>) LogCommon::error, ERROR),
            Arguments.of((BiConsumer<String, Object[]>) LogCommon::debug, DEBUG),
            Arguments.of((BiConsumer<String, Object[]>) LogCommon::trace, TRACE),
            Arguments.of((BiConsumer<String, Object[]>) LogCommon::step, STEP),
            Arguments.of((BiConsumer<String, Object[]>) LogCommon::extended, EXTENDED)
      );
   }

   private static String formatMessage(String methodName) {
      return "Test " + methodName + " message";
   }

   private static void verifyLogMethod(LogCommon mockInstance, String methodName, String message, Object[] args) {
      switch (methodName) {
         case INFO -> verify(mockInstance).infoLog(message, args);
         case WARN -> verify(mockInstance).warnLog(message, args);
         case ERROR -> verify(mockInstance).errorLog(message, args);
         case DEBUG -> verify(mockInstance).debugLog(message, args);
         case TRACE -> verify(mockInstance).traceLog(message, args);
         case STEP -> verify(mockInstance).stepLog(message, args);
         case EXTENDED -> verify(mockInstance).extendedLog(message, args);
         default -> throw new IllegalArgumentException("Unexpected method: " + methodName);
      }
   }

   private static Field getInstanceField() throws NoSuchFieldException {
      Field instanceField = LogCommon.class.getDeclaredField(INSTANCE_FIELD);
      instanceField.setAccessible(true);
      return instanceField;
   }
}