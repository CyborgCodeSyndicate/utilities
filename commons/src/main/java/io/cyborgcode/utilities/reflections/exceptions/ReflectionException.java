package io.cyborgcode.utilities.reflections.exceptions;

/**
 * Exception thrown when a reflection-based operation fails.
 *
 * <p>This exception acts as a wrapper around reflection-related errors, providing
 * more meaningful error messages for debugging and troubleshooting.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public class ReflectionException extends RuntimeException {

   /**
    * Constructs a new ReflectionException with the specified message.
    *
    * @param message The detailed error message explaining the cause of failure.
    */
   public ReflectionException(String message) {
      super(message);
   }

   /**
    * Constructs a new ReflectionException with the specified message and cause.
    *
    * @param message The detailed error message explaining the cause of failure.
    * @param cause   The underlying exception that caused this reflection failure.
    */
   public ReflectionException(String message, Throwable cause) {
      super(message, cause);
   }

}
