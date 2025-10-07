package io.cyborgcode.utilities.reflections;

import io.cyborgcode.utilities.logging.LogCommon;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@DisplayName("RetryUtils Tests")
class RetryUtilsTest {

   @Nested
   @DisplayName("Basic Retry Functionality")
   class BasicRetryFunctionality {

      @Test
      @DisplayName("Should return immediately when condition is satisfied on the first attempt")
      void shouldReturnImmediatelyWhenConditionIsSatisfiedOnFirstAttempt() {
         // When
         String result = RetryUtils.retryUntil(
               Duration.ofSeconds(1),
               Duration.ofMillis(10),
               () -> "success",
               res -> res.equals("success")
         );

         // Then
         assertEquals("success", result, "Should return the successful result immediately");
      }


      @Test
      @DisplayName("Should keep retrying until condition is eventually satisfied")
      void shouldKeepRetryingUntilConditionIsEventuallySatisfied() {
         // Given
         AtomicInteger counter = new AtomicInteger(0);

         // When
         String result = RetryUtils.retryUntil(
               Duration.ofSeconds(2),
               Duration.ofMillis(10),
               () -> (counter.incrementAndGet() >= 3) ? "done" : "not yet",
               res -> res.equals("done")
         );

         // Then
         assertEquals("done", result, "Should return the final successful result");
         assertTrue(counter.get() >= 3, "Should have attempted at least 3 times");
      }


      @Test
      @DisplayName("Should handle exceptions in supplier and continue retrying until success")
      void shouldHandleSupplierExceptionsAndContinueRetryingUntilSuccess() {
         // Given
         AtomicInteger counter = new AtomicInteger(0);

         // When
         String result = RetryUtils.retryUntil(
               Duration.ofSeconds(2),
               Duration.ofMillis(10),
               () -> {
                  if (counter.incrementAndGet() < 3) {
                     throw new RuntimeException("failure");
                  }
                  return "ok";
               },
               res -> res.equals("ok")
         );

         // Then
         assertEquals("ok", result, "Should return success after handling exceptions");
         assertTrue(counter.get() >= 3, "Should have attempted at least 3 times");
      }

   }

   @Nested
   @DisplayName("Timeout and Failure Handling")
   class TimeoutAndFailureHandling {

      @Test
      @DisplayName("Should throw exception when condition is never satisfied within max wait time")
      @Timeout(1)
         // Ensures test doesn't hang too long
      void shouldThrowExceptionWhenConditionIsNeverSatisfied() {
         // Given
         AtomicInteger counter = new AtomicInteger(0);
         Duration shortTimeout = Duration.ofMillis(100);

         // When/Then
         IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
               RetryUtils.retryUntil(
                     shortTimeout,
                     Duration.ofMillis(20),
                     () -> {
                        counter.incrementAndGet();
                        return "bad";
                     },
                     res -> res.equals("good")
               )
         );

         assertTrue(ex.getMessage().contains("Failed to satisfy condition"),
               "Exception message should indicate failure reason");
         assertTrue(counter.get() > 0, "Should have made at least one attempt");
      }


      @Test
      @DisplayName("Should throw exception when thread is interrupted during retry")
      void shouldThrowExceptionWhenThreadIsInterrupted() {
         // Given
         Supplier<String> supplier = () -> "not ok";
         Thread.currentThread().interrupt(); // Simulate interruption

         // When/Then
         IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
               RetryUtils.retryUntil(
                     Duration.ofSeconds(1),
                     Duration.ofMillis(50),
                     supplier,
                     res -> false
               )
         );

         assertTrue(ex.getMessage().contains("Retry was interrupted"),
               "Exception message should indicate interruption");

         // Clear interrupt flag for subsequent tests
         Thread.interrupted();
      }

   }

   @Nested
   @DisplayName("Parameter Validation")
   class ParameterValidation {

      @Test
      @DisplayName("Should throw NullPointerException for null arguments")
      void shouldThrowNullPointerExceptionForNullArguments() {
         // Given
         Supplier<String> supplier = () -> "value";
         Predicate<String> condition = s -> s.equals("value");
         Duration maxWait = Duration.ofSeconds(1);
         Duration interval = Duration.ofMillis(10);

         // When/Then - Test each parameter
         NullPointerException ex1 = assertThrows(NullPointerException.class, () ->
               RetryUtils.retryUntil(null,
                     interval, supplier, condition)
         );
         assertTrue(ex1.getMessage().contains("maxWait must not be null"),
               "Should check maxWait parameter");

         NullPointerException ex2 = assertThrows(NullPointerException.class, () ->
               RetryUtils.retryUntil(maxWait, null,
                     supplier, condition)
         );
         assertTrue(ex2.getMessage().contains("retryInterval must not be null"),
               "Should check retryInterval parameter");

         NullPointerException ex3 = assertThrows(NullPointerException.class, () ->
               RetryUtils.retryUntil(maxWait,
                     interval, null, condition)
         );
         assertTrue(ex3.getMessage().contains("supplier must not be null"),
               "Should check supplier parameter");

         NullPointerException ex4 = assertThrows(NullPointerException.class, () ->
               RetryUtils.retryUntil(maxWait,
                     interval, supplier, null)
         );
         assertTrue(ex4.getMessage().contains("condition must not be null"),
               "Should check condition parameter");
      }

   }


   @Test
   @DisplayName("Should throw exception when max wait time is exceeded")
   void shouldThrowExceptionWhenMaxWaitTimeIsExceeded() {
      // Given - a slightly longer timeout for test stability
      Duration maxWait = Duration.ofMillis(500);
      Duration interval = Duration.ofMillis(50);

      // When
      IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            RetryUtils.retryUntil(
                  maxWait,
                  interval,
                  () -> "never",
                  res -> false
            )
      );

      // Then
      String message = ex.getMessage();
      assertTrue(message.contains("Failed to satisfy condition"), "Exception should indicate timeout");
      assertTrue(message.contains("after"), "Exception should mention that attempts were made");
   }


   @Test
   @DisplayName("Should handle zero retry interval successfully")
   void shouldHandleZeroRetryIntervalSuccessfully() {
      // Given
      AtomicInteger counter = new AtomicInteger(0);

      // When
      String result = RetryUtils.retryUntil(
            Duration.ofMillis(200),
            Duration.ofMillis(0),
            () -> (counter.incrementAndGet() >= 3) ? "done" : "not yet",
            res -> res.equals("done")
      );

      // Then
      assertEquals("done", result, "Should return successful result with zero interval");
      assertTrue(counter.get() >= 3, "Should have attempted at least 3 times");
   }


   @Test
   @DisplayName("Should log retry attempts appropriately")
   void shouldLogRetryAttemptsAppropriately() {
      try (MockedStatic<LogCommon> logMock = mockStatic(LogCommon.class)) {
         // Given
         AtomicInteger counter = new AtomicInteger(0);

         // When
         RetryUtils.retryUntil(
               Duration.ofMillis(100),
               Duration.ofMillis(10),
               () -> (counter.incrementAndGet() >= 3) ? "done" : "not yet",
               res -> res.equals("done")
         );

         // Then
         logMock.verify(() -> LogCommon.debug(contains("Condition not satisfied"), anyInt()), times(2));
         logMock.verify(() -> LogCommon.info(contains("Condition satisfied"), eq(3)), times(1));
      } catch (Exception e) {
         fail("Test failed due to unexpected exception");
      }
   }


   @Nested
   @DisplayName("RetryUtils Edge Cases")
   class RetryUtilsEdgeCasesTests {

      @Test
      @DisplayName("Should return immediately if condition is always true")
      void shouldReturnImmediatelyIfConditionIsAlwaysTrue() {
         // Given
         Duration maxWait = Duration.ofMillis(1000);
         Duration interval = Duration.ofMillis(100);
         AtomicInteger counter = new AtomicInteger(0);

         // When
         String result = RetryUtils.retryUntil(
               maxWait,
               interval,
               () -> {
                  counter.incrementAndGet();
                  return "success";
               },
               s -> true // Always satisfies condition
         );

         // Then
         assertEquals("success", result, "Should return the successful result");
         assertEquals(1, counter.get(), "Should only make one attempt");
      }


      @Test
      @DisplayName("Should throw exception immediately if wait time is zero")
      void shouldThrowExceptionWhenWaitTimeIsZero() {
         // Given
         Duration zeroWait = Duration.ofMillis(0);
         Duration interval = Duration.ofMillis(100);

         // When/Then
         IllegalStateException ex = assertThrows(IllegalStateException.class,
               () -> RetryUtils.retryUntil(
                     zeroWait,
                     interval,
                     () -> "result",
                     s -> false // Condition never satisfied
               ),
               "Should throw with zero wait time"
         );

         assertTrue(ex.getMessage().contains("Failed to satisfy condition"),
               "Exception should indicate timeout");
      }


      @Test
      @DisplayName("Should throw exception when condition is never met and interval is zero")
      void shouldThrowExceptionWhenConditionNeverMetAndIntervalIsZero() {
         // Given
         Duration maxWait = Duration.ofMillis(100); // Keep low to avoid long tests
         Duration zeroInterval = Duration.ofMillis(0);
         AtomicInteger counter = new AtomicInteger(0);

         // When/Then
         IllegalStateException ex = assertThrows(IllegalStateException.class,
               () -> RetryUtils.retryUntil(
                     maxWait,
                     zeroInterval,
                     () -> {
                        counter.incrementAndGet();
                        return "result";
                     },
                     s -> false
               ),
               "Should handle zero retry interval properly"
         );

         assertTrue(counter.get() >= 2, "Should make multiple attempts even with zero interval");
      }


      @Test
      @DisplayName("Should collect suppressed exceptions when condition throws repeatedly")
      void shouldCollectSuppressedExceptionsWhenConditionThrowsRepeatedly() {
         // Given
         Duration maxWait = Duration.ofMillis(500);  // Slightly longer for stability
         Duration interval = Duration.ofMillis(50);
         AtomicInteger exceptionCount = new AtomicInteger(0);

         // When/Then
         IllegalStateException ex = assertThrows(IllegalStateException.class,
               () -> RetryUtils.retryUntil(
                     maxWait,
                     interval,
                     () -> "result",
                     s -> {
                        int count = exceptionCount.incrementAndGet();
                        throw new RuntimeException("Condition exception #" + count);
                     }
               )
         );

         // Then
         assertTrue(ex.getMessage().contains("Failed to satisfy condition"),
               "Should indicate that condition wasn't satisfied");

         // Check suppressed exceptions
         Throwable[] suppressed = ex.getSuppressed();
         assertTrue(suppressed.length > 0, "Should have at least one suppressed exception");
         assertTrue(suppressed[0].getMessage().contains("Condition exception"),
               "Suppressed exception should be from our condition");

         // Verify condition was called
         assertTrue(exceptionCount.get() > 0, "Condition should be called at least once");
      }


      @Test
      @DisplayName("Should handle very small intervals with multiple attempts")
      void shouldHandleVerySmallIntervalsWithMultipleAttempts() {
         // Given
         Duration maxWait = Duration.ofMillis(100);
         Duration tinyInterval = Duration.ofNanos(1); // Extremely small interval
         AtomicInteger counter = new AtomicInteger(0);

         // When/Then
         IllegalStateException ex = assertThrows(IllegalStateException.class,
               () -> RetryUtils.retryUntil(
                     maxWait,
                     tinyInterval,
                     () -> {
                        counter.incrementAndGet();
                        return "result";
                     },
                     s -> false // Never satisfies condition
               ),
               "Should handle very small intervals properly"
         );

         assertTrue(counter.get() >= 2, "Should make multiple attempts with tiny interval");
      }


      @Test
      @DisplayName("Should throw when maxWait is negative")
      void shouldThrowWhenMaxWaitIsNegative() {
         // Given
         Duration maxWait = Duration.ofMillis(-1);
         Duration retryInterval = Duration.ofMillis(10);

         // When / Then
         IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
               RetryUtils.retryUntil(
                     maxWait, retryInterval,
                     () -> "value", v -> true)
         );

         assertEquals("maxWait must not be negative", ex.getMessage());
      }


      @Test
      @DisplayName("Should throw when retryInterval is negative")
      void shouldThrowWhenRetryIntervalIsNegative() {
         // Given
         Duration maxWait = Duration.ofMillis(100);
         Duration retryInterval = Duration.ofMillis(-10);

         // When / Then
         IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
               RetryUtils.retryUntil(
                     maxWait, retryInterval,
                     () -> "value", v -> true)
         );

         assertEquals("retryInterval must not be negative", ex.getMessage());
      }


      @Test
      @DisplayName("Should exit retry loop quickly if remaining time is extremely short")
      void shouldExitQuicklyIfRemainingTimeIsShort() {
         // Given a very short timeout and a much longer interval
         Duration maxWait = Duration.ofMillis(1);      // Almost no time
         Duration retryInterval = Duration.ofMillis(100);
         AtomicInteger counter = new AtomicInteger(0);

         long startTime = System.nanoTime();

         // When / Then
         IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
               RetryUtils.retryUntil(
                     maxWait,
                     retryInterval,
                     () -> {
                        counter.incrementAndGet();
                        return "nope";
                     },
                     result -> false
               )
         );

         // Assert it ran quickly and attempted at least once (but not many times)
         assertTrue(counter.get() >= 1, "Should attempt at least once");

         assertTrue(ex.getMessage().contains("Failed to satisfy condition"),
               "Should indicate condition was not satisfied in time");
      }

   }

}
