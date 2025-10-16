package io.cyborgcode.utilities.reflections;

import io.cyborgcode.utilities.reflections.exceptions.ReflectionException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.reflections.Reflections;

/**
 * Utility class for performing advanced reflection-based operations.
 *
 * <p>This class provides methods to dynamically discover class implementations, retrieve
 * field values, and interact with class hierarchies using reflection.
 * It facilitates working with enums, interfaces, and generic field retrieval.
 *
 * <p>Features:
 * <ul>
 *     <li>Finding implementations of interfaces or subclasses in a given package.</li>
 *     <li>Retrieving private or protected field values from objects.</li>
 *     <li>Handling enums that implement interfaces dynamically.</li>
 *     <li>Safeguarding reflection operations with structured exception handling.</li>
 * </ul>
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public class ReflectionUtil {

   private ReflectionUtil() {
   }


   /**
    * Finds all enum classes that implement a given interface within a specified package.
    *
    * @param interfaceClass The interface whose enum implementations are being searched for. Must not be null.
    * @param packagePrefix  The root package to search within. Must not be null.
    * @param <T>            The type of the interface.
    * @return A list of enum classes that implement the specified interface.
    * @throws ReflectionException If no matching enum classes are found or if the search fails.
    */
   @SuppressWarnings("unchecked")
   public static <T> List<Class<? extends Enum>> findEnumClassImplementationsOfInterface(
         Class<T> interfaceClass, String packagePrefix) {

      validateInputs(interfaceClass, packagePrefix);

      Reflections reflections = new Reflections(packagePrefix);
      Set<Class<? extends T>> result = reflections.getSubTypesOf(interfaceClass);

      List<Class<? extends Enum>> listOfEnumClasses = new ArrayList<>();
      for (Class<? extends T> cls : result) {
         if (cls.isEnum()) {
            @SuppressWarnings("unchecked")
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) cls;
            listOfEnumClasses.add(enumClass);
         }
      }

      if (listOfEnumClasses.isEmpty()) {
         throw new ReflectionException(String.format(
               "No Enum implementing interface '%s' found in package '%s'.",
               interfaceClass.getName(), packagePrefix));
      }
      return listOfEnumClasses;
   }

   /**
    * Finds a specific enum constant that implements a given interface.
    *
    * @param interfaceClass The interface implemented by the enum.
    * @param enumName       The name of the enum constant.
    * @param packagePrefix  The package to search within.
    * @param <T>            The type of the interface.
    * @return The matching enum constant.
    * @throws ReflectionException If the enum or constant is not found.
    */
   @SuppressWarnings("unchecked")
   public static <T> T findEnumImplementationsOfInterface(
         Class<T> interfaceClass, String enumName, String packagePrefix) {

      List<Class<? extends Enum>> enumClassImplementationsOfInterface =
            findEnumClassImplementationsOfInterface(interfaceClass, packagePrefix);

      List<? extends Enum> enumValuesList = enumClassImplementationsOfInterface.stream()
            .flatMap(enumClass -> Arrays.stream(enumClass.getEnumConstants()))
            .filter(anEnum -> anEnum.name().equals(enumName)).toList();

      if (enumValuesList.isEmpty()) {
         throw new ReflectionException(String.format(
               "Enum value '%s' not found in any enum class.", enumName));
      }
      if (enumValuesList.size() > 1) {
         throw new ReflectionException(String.format(
               "There are more than one enum with value '%s'.", enumName));
      }
      return (T) enumValuesList.get(0);
   }

   /**
    * Finds all class implementations of a given interface within a package.
    *
    * @param interfaceClass The interface whose implementations are to be found.
    * @param packagePrefix  The package to search within.
    * @param <T>            The type of the interface.
    * @return A list of classes implementing the specified interface.
    */
   public static <T> List<Class<? extends T>> findImplementationsOfInterface(Class<T> interfaceClass,
                                                                             String packagePrefix) {
      validateInputs(interfaceClass, packagePrefix);

      Reflections reflections = new Reflections(packagePrefix);
      Set<Class<? extends T>> result = reflections.getSubTypesOf(interfaceClass);
      return new ArrayList<>(result);
   }

   /**
    * Retrieves all field values of a specified type from an object, including fields declared in superclasses.
    *
    * @param instance  The object whose field values are being retrieved. Must not be null.
    * @param fieldType The expected type of the fields to retrieve. Must not be null.
    * @param <K>       The type parameter representing the expected field value type.
    * @return A list of field values of the specified type found in the object.
    * @throws ReflectionException If no matching fields are found, if a field contains an incompatible value,
    *                             or if a field cannot be accessed due to security restrictions.
    */
   @SuppressWarnings("java:S3011")
   public static <K> List<K> getFieldValues(Object instance, Class<K> fieldType) {
      validateInputs(instance, fieldType);

      try {
         Class<?> currentClass = instance.getClass();
         List<Field> allFields = new ArrayList<>();

         // Traverse class hierarchy to collect all declared fields
         while (currentClass != null && currentClass != Object.class) {
            allFields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
         }

         List<K> matchingValues = new ArrayList<>();

         for (Field field : allFields) {
            field.setAccessible(true);

            // Check if the declared field type is compatible
            if (fieldType.isAssignableFrom(field.getType())) {
               Object value = field.get(instance);

               if (fieldType.isInstance(value)) {
                  matchingValues.add(fieldType.cast(value));
               } else {
                  throw new ReflectionException(String.format(
                        "Field '%s' in class '%s' is declared as assignable to '%s' but holds an incompatible "
                              + "value of type '%s'.",
                        field.getName(),
                        instance.getClass().getName(),
                        fieldType.getName(),
                        value != null ? value.getClass().getName() : "null"
                  ));
               }
            }
         }

         if (matchingValues.isEmpty()) {
            throw new ReflectionException(String.format(
                  "No fields of type '%s' found in class '%s'.",
                  fieldType.getName(), instance.getClass().getName()));
         }

         return matchingValues;

      } catch (IllegalAccessException e) {
         throw new ReflectionException(String.format(
               "Cannot access field(s) of type '%s' in class '%s'.",
               fieldType.getName(), instance.getClass().getName()), e);
      }
   }

   /**
    * Validates input parameters, ensuring they are non-null and non-empty.
    *
    * @param objects The objects to validate.
    * @throws IllegalArgumentException If any parameter is invalid.
    */
   private static void validateInputs(Object... objects) {
      for (Object obj : objects) {
         if (obj == null) {
            throw new IllegalArgumentException("Input parameter cannot be null.");
         }
         if (obj instanceof String str && str.trim().isEmpty()) {
            throw new IllegalArgumentException("String input parameter cannot be empty.");
         }
      }
   }

}
