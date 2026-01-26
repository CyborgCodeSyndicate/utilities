package io.cyborgcode.utilities.reflections.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@DisplayName("ReflectionException Tests")
class ReflectionExceptionTest {

   private static final String TEST_MESSAGE = "Test message";
   private static final String CAUSE_MESSAGE = "Cause";

   @Test
   @DisplayName("Constructor with message should set message correctly")
   void testConstructorWithMessage() {
      // When
      ReflectionException exception = new ReflectionException(TEST_MESSAGE);

      // Then
      assertEquals(TEST_MESSAGE, exception.getMessage(), "Message should match constructor parameter");
   }

   @Test
   @DisplayName("Constructor with message and cause should set both correctly")
   void testConstructorWithMessageAndCause() {
      // Given
      Throwable cause = new RuntimeException(CAUSE_MESSAGE);

      // When
      ReflectionException exception = new ReflectionException(TEST_MESSAGE, cause);

      // Then
      assertAll(
            () -> assertEquals(TEST_MESSAGE, exception.getMessage(), "Message should match constructor parameter"),
            () -> assertSame(cause, exception.getCause(), "Cause should match constructor parameter")
      );
   }
}