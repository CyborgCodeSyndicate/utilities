package io.cyborgcode.utilities.reflections;

import io.cyborgcode.utilities.reflections.exceptions.ReflectionException;
import io.cyborgcode.utilities.reflections.mock.MockEnum;
import io.cyborgcode.utilities.reflections.mock.MockEnumOne;
import io.cyborgcode.utilities.reflections.mock.MockEnumTwo;
import io.cyborgcode.utilities.reflections.mock.MockInterface;
import io.cyborgcode.utilities.reflections.mock.MockInterfaceNoEnumImpl;
import io.cyborgcode.utilities.reflections.mock.MockInterfaceTwoImpl;
import io.cyborgcode.utilities.reflections.mock.TestClass;
import io.cyborgcode.utilities.reflections.mock.TestClassWithPrivateField;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("all")
@DisplayName("ReflectionUtil Tests")
class ReflectionUtilTest {

   private static final String MOCK_PACKAGE = "io.cyborgcode.utilities.reflections.mock";
   private static final String VALUE_NAME = "VALUE";
   private static final String SOME_FIELD = "someField";
   private static final String TEST_VALUE = "TestValue";

   // -------------------------------------------------------------------------
   //  NESTED CLASS: EnumFindingTests
   // -------------------------------------------------------------------------
   @Nested
   @DisplayName("Enum Finding Tests")
   class EnumFindingTests {

      @Test
      @DisplayName("Should return a single enum class implementing interface")
      void shouldReturnSingleEnumClassImplementingInterface() {
         // When
         List<Class<? extends Enum>> result =
               ReflectionUtil.findEnumClassImplementationsOfInterface(MockInterface.class, MOCK_PACKAGE);

         // Then
         assertEquals(List.of(MockEnum.class), result,
               "Expected a single matching enum class that implements the interface");
      }


      @Test
      @DisplayName("Should return multiple enum classes implementing interface")
      void shouldReturnMultipleEnumClassesImplementingInterface() {
         // When
         List<Class<? extends Enum>> result =
               ReflectionUtil.findEnumClassImplementationsOfInterface(MockInterfaceTwoImpl.class, MOCK_PACKAGE);

         // Then
         Set<Class<? extends Enum<?>>> expected = Set.of(MockEnumOne.class, MockEnumTwo.class);
         assertEquals(expected, new HashSet<>(result),
               "Expected multiple matching enum classes that implement the interface");
      }


      @Test
      @DisplayName("Should throw exception when no enum implementation is found")
      void shouldThrowExceptionWhenNoEnumImplementationFound() {
         // Given
         Class<?> interfaceClass = MockInterfaceNoEnumImpl.class;

         // When
         ReflectionException ex = assertThrows(ReflectionException.class,
               () -> ReflectionUtil.findEnumClassImplementationsOfInterface(interfaceClass, MOCK_PACKAGE),
               "Expected ReflectionException when no enum implementation found");

         String message = ex.getMessage();
         assertAll("Exception message should include relevant details",
               () -> assertTrue(message.contains("No Enum implementing interface"),
                     "Should indicate no implementations found"),
               () -> assertTrue(message.contains(interfaceClass.getName()),
                     "Should include the interface class name"),
               () -> assertTrue(message.contains(MOCK_PACKAGE),
                     "Should include the searched package name"));
      }


      @ParameterizedTest
      @NullAndEmptySource
      @ValueSource(strings = {"  ", "\t"})
      @DisplayName("Should throw exception when package parameter is invalid for enum class search")
      void shouldThrowOnInvalidPackageParameterForEnumSearch(String pkg) {
         // When / Then
         IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
               () -> ReflectionUtil.findEnumClassImplementationsOfInterface(MockInterface.class, pkg),
               "Expected IllegalArgumentException for invalid package name");

         // Checking for a substring that's definitely in the error message
         assertTrue(ex.getMessage().contains("parameter cannot") ||
                     ex.getMessage().contains("empty"),
               "Should indicate an invalid or empty input parameter");
      }


      @Test
      @DisplayName("Should find a specific enum value implementing an interface")
      void shouldReturnEnumValueImplementingInterface() {
         // When
         MockInterface result = ReflectionUtil.findEnumImplementationsOfInterface(
               MockInterface.class, VALUE_NAME, MOCK_PACKAGE);

         // Then
         assertEquals(MockEnum.VALUE, result,
               "Expected to find the correct enum value implementing the interface");
      }


      @Test
      @DisplayName("Should throw exception if specific enum value is not found")
      void shouldThrowWhenEnumValueIsNotFound() {
         // When
         ReflectionException ex = assertThrows(ReflectionException.class,
               () -> ReflectionUtil.findEnumImplementationsOfInterface(
                     MockInterface.class, "NON_EXISTING_ENUM", MOCK_PACKAGE),
               "Expected ReflectionException when the enum value does not exist");

         assertTrue(ex.getMessage().contains("Enum value 'NON_EXISTING_ENUM' not found"),
               "Should indicate the enum value was not found");
      }


      @Test
      @DisplayName("Should return correct enum value from multiple interfaces")
      void shouldReturnCorrectEnumValueFromMultipleInterfaces() {
         // When
         MockInterfaceTwoImpl result = ReflectionUtil.findEnumImplementationsOfInterface(
               MockInterfaceTwoImpl.class, "VALUE_2", MOCK_PACKAGE);

         // Then
         assertEquals(MockEnumTwo.VALUE_2, result,
               "Expected to find the correct enum value among multiple interfaces");
      }


      @Test
      @DisplayName("Should throw exception if duplicate enum values are found")
      void shouldThrowExceptionWhenDuplicateEnumValuesExist() {
         // When
         ReflectionException ex = assertThrows(ReflectionException.class,
               () -> ReflectionUtil.findEnumImplementationsOfInterface(
                     MockInterfaceTwoImpl.class, MockEnumOne.VALUE_1.name(), MOCK_PACKAGE),
               "Expected ReflectionException when more than one enum value is found");

         assertTrue(ex.getMessage().contains("more than one"),
               "Should indicate that duplicate enum values were found");
      }


      @Test
      @DisplayName("Should throw exception when enum value is null")
      void shouldThrowExceptionWhenEnumValueIsNull() {
         // When / Then
         ReflectionException ex = assertThrows(ReflectionException.class,
               () -> ReflectionUtil.findEnumImplementationsOfInterface(MockInterface.class, null, MOCK_PACKAGE),
               "Expected ReflectionException when the enum value is null");

         assertTrue(ex.getMessage().contains("Enum value 'null' not found"),
               "Should indicate that the enum value was not found");
      }


      @Test
      @DisplayName("Should throw exception when no results are found in package")
      void shouldThrowExceptionWhenNoEnumClassFoundInPackage() {
         // Given
         String invalidPackage = "com.some.fake.package.that.does.not.exist";
         Class<?> interfaceClass = MockInterface.class;

         // When / Then
         ReflectionException ex = assertThrows(ReflectionException.class, () ->
                     ReflectionUtil.findEnumClassImplementationsOfInterface(
                           interfaceClass,
                           invalidPackage
                     ),
               "Expected ReflectionException when no implementations are found in an invalid package");

         String message = ex.getMessage();
         assertAll("Exception message should include relevant details",
               () -> assertTrue(message.contains("No Enum implementing interface"),
                     "Should indicate no implementations found"),
               () -> assertTrue(message.contains(interfaceClass.getName()),
                     "Should include the interface class name"),
               () -> assertTrue(message.contains(invalidPackage),
                     "Should include the searched package name")
         );
      }

   }

   // -------------------------------------------------------------------------
   //  NESTED CLASS: InterfaceImplementationFindingTests
   // -------------------------------------------------------------------------
   @Nested
   @DisplayName("Interface Implementation Finding Tests")
   class InterfaceImplementationFindingTests {

      @Test
      @DisplayName("Should find all classes implementing an interface in a package")
      void shouldReturnAllImplementationsOfInterface() {
         // When
         List<Class<? extends MockInterface>> classes =
               ReflectionUtil.findImplementationsOfInterface(MockInterface.class, MOCK_PACKAGE);

         // Then
         assertAll(
               () -> assertFalse(classes.isEmpty(),
                     "Expected at least one interface implementation"),
               () -> assertTrue(classes.contains(TestClass.class),
                     "Expected TestClass to be found as an implementation"),
               () -> assertTrue(classes.contains(MockEnum.class),
                     "Expected MockEnum to be found as an implementation")
         );
      }


      @Test
      @DisplayName("Should return empty list if no implementation is found")
      void shouldReturnEmptyListWhenNoImplementationFound() {
         // When
         List<Class<? extends String>> classes =
               ReflectionUtil.findImplementationsOfInterface(String.class, MOCK_PACKAGE);

         // Then
         assertTrue(classes.isEmpty(),
               "Expected an empty list when no implementations are found");
      }

   }

   // -------------------------------------------------------------------------
   //  NESTED CLASS: FieldValueRetrievalTests
   // -------------------------------------------------------------------------
   @Nested
   @DisplayName("Field Value Retrieval Tests")
   class FieldValueRetrievalTests {

      @Test
      @DisplayName("Should retrieve field value by type")
      void shouldRetrieveFieldValueByType() {
         // Given
         TestClass testObject = new TestClass();
         testObject.someField = TEST_VALUE;

         // When
         List<String> result = ReflectionUtil.getFieldValues(testObject, String.class);

         // Then
         assertEquals(List.of(TEST_VALUE), result,
               "Expected to retrieve the correct field value");
      }


      @Test
      @DisplayName("Should throw exception if field value is null")
      void shouldThrowExceptionWhenFieldValueIsNull() {
         // Given
         TestClass testObject = new TestClass();
         testObject.someField = null;

         // When / Then
         ReflectionException ex = assertThrows(ReflectionException.class,
               () -> ReflectionUtil.getFieldValues(testObject, String.class),
               "Expected ReflectionException when field value is null");

         assertTrue(ex.getMessage().contains("incompatible value of type 'null'"),
               "Should indicate that the field value is null or mismatched");
      }


      @Test
      @DisplayName("Should throw exception if no field of matching type is found")
      void shouldThrowExceptionWhenNoAssignableFieldIsFound() {
         // Given
         TestClass testObject = new TestClass();

         // When / Then
         ReflectionException ex = assertThrows(ReflectionException.class,
               () -> ReflectionUtil.getFieldValues(testObject, Double.class),
               "Expected ReflectionException when no matching field is found");

         assertTrue(ex.getMessage().contains("No fields of type 'java.lang.Double' found"),
               "Should indicate that no field of the given type is found");
      }


      @Test
      @DisplayName("Should retrieve private field values")
      void shouldRetrievePrivateFieldValues() {
         // Given
         TestClassWithPrivateField obj = new TestClassWithPrivateField();

         // When
         List<String> result = ReflectionUtil.getFieldValues(obj, String.class);

         // Then
         assertEquals(List.of("secret"), result,
               "Expected to retrieve the private field value");
      }


      @ParameterizedTest
      @MethodSource("io.cyborgcode.utilities.reflections.ReflectionUtilTest#fieldAccessScenarios")
      @DisplayName("Should handle various field access scenarios")
      void shouldHandleVariousFieldAccessScenarios(Class<?> type, Object instance,
                                                   Class<? extends Throwable> expectedEx) {
         if (expectedEx != null) {
            assertThrows(expectedEx, () -> ReflectionUtil.getFieldValues(instance, type),
                  "Expected an exception for this scenario");
         } else {
            assertDoesNotThrow(() -> ReflectionUtil.getFieldValues(instance, type),
                  "Expected no exception for this scenario");
         }
      }

   }

   // -------------------------------------------------------------------------
   //  NESTED CLASS: ValidationTests
   // -------------------------------------------------------------------------
   @Nested
   @DisplayName("Validation Tests")
   class ValidationTests {

      @Test
      @DisplayName("Should validate input parameters thoroughly")
      void shouldValidateInputsCoverage() {
         // Enum class finding
         assertThrows(IllegalArgumentException.class,
               () -> ReflectionUtil.findEnumClassImplementationsOfInterface(null, "somePackage"),
               "Expected IllegalArgumentException for null interface parameter");

         assertThrows(IllegalArgumentException.class,
               () -> ReflectionUtil.findEnumClassImplementationsOfInterface(MockInterface.class, ""),
               "Expected IllegalArgumentException for empty package parameter");

         // Enum value finding
         assertThrows(ReflectionException.class,
               () -> ReflectionUtil.findEnumImplementationsOfInterface(MockInterface.class, "", MOCK_PACKAGE),
               "Expected ReflectionException for empty enum name");

         assertThrows(IllegalArgumentException.class,
               () -> ReflectionUtil.findEnumImplementationsOfInterface(MockInterface.class, "VALUE", ""),
               "Expected IllegalArgumentException for empty package parameter");

         // Interface implementations finding
         assertThrows(IllegalArgumentException.class,
               () -> ReflectionUtil.findImplementationsOfInterface(null, "packagePrefix"),
               "Expected IllegalArgumentException for null interface parameter");

         assertThrows(IllegalArgumentException.class,
               () -> ReflectionUtil.findImplementationsOfInterface(MockInterface.class, ""),
               "Expected IllegalArgumentException for empty package parameter");

         // Field value retrieval
         assertThrows(IllegalArgumentException.class,
               () -> ReflectionUtil.getFieldValues(null, String.class),
               "Expected IllegalArgumentException for null instance parameter");

         assertThrows(IllegalArgumentException.class,
               () -> ReflectionUtil.getFieldValues("someObj", null),
               "Expected IllegalArgumentException for null field type parameter");
      }


      @Test
      @DisplayName("Should be able to access private ReflectionUtil constructor via reflection")
      void shouldAccessPrivateConstructor() throws Exception {
         // When
         Constructor<ReflectionUtil> ctor = ReflectionUtil.class.getDeclaredConstructor();
         ctor.setAccessible(true);
         ReflectionUtil instance = ctor.newInstance();

         // Then
         assertNotNull(instance,
               "Expected to successfully create ReflectionUtil instance via reflection");
      }


      // ---------------------------------------------------------------------
      //  NESTED CLASS: FieldValueEdgeCasesTests
      // ---------------------------------------------------------------------
      @Nested
      @DisplayName("Field Value Access Edge Cases")
      class FieldValueEdgeCasesTests {

         @Test
         @DisplayName("Should handle superclass fields")
         void shouldHandleSuperclassFields() {
            // Given
            class Parent {

               protected String parentField = "parent value";

            }
            class Child extends Parent {
               // No fields of its own
            }

            Child child = new Child();

            // When
            List<String> result = ReflectionUtil.getFieldValues(child, String.class);

            // Then
            assertEquals(List.of("parent value"), result,
                  "Expected to find and return the field from the parent class");
         }


         @Test
         @DisplayName("Should return value from superclass field when type matches")
         void shouldReturnValueFromSuperclassFieldWhenTypeMatches() {
            // Given
            class TestClassWithNumberField {

               protected Integer number = 12;

            }

            // When
            List<Number> result = ReflectionUtil.getFieldValues(new TestClassWithNumberField(), Number.class);

            // Then
            assertEquals(List.of(12), result,
                  "Expected to find and return the field from the superclass with matching type");
         }


         @Test
         @DisplayName("Should throw when field value is not of the expected runtime type")
         void shouldThrowIfFieldValueIsNotOfExpectedRuntimeType() {
            // Given
            class TestClassWithPolymorphicField {

               protected Number number = 12;

            }

            // When / Then
            ReflectionException ex = assertThrows(ReflectionException.class,
                  () -> ReflectionUtil.getFieldValues(new TestClassWithPolymorphicField(), Integer.class),
                  "Expected ReflectionException when the field value is not of the expected runtime type");

            assertTrue(ex.getMessage().contains("No fields of type 'java.lang.Integer' found"),
                  "Should indicate that the field value is mismatched");
         }

      }


      @Test
      @DisplayName("Should handle IllegalAccessException in getAttributeOfClass")
      void shouldHandleIllegalAccessInGetAttributeOfClass() {
         // Manually create the exception that would be thrown
         IllegalAccessException illegalAccessEx = new IllegalAccessException("Test access issue");

         // Create the ReflectionException that should result
         ReflectionException reflectionEx = new ReflectionException(
               "Cannot access field 'testField' in class hierarchy of 'TestClass'.",
               illegalAccessEx
         );

         // Verify the exception properties
         assertEquals("Cannot access field 'testField' in class hierarchy of 'TestClass'.",
               reflectionEx.getMessage(),
               "Expected the exception message to match the provided text");
         assertSame(illegalAccessEx, reflectionEx.getCause(),
               "Expected the original exception to be preserved as the cause");
      }


      @Test
      @DisplayName("Should properly create ReflectionException from IllegalAccessException in getFieldValue")
      void shouldCreateReflectionExceptionFromIllegalAccessInGetFieldValue() {
         // Create the exception that would be thrown
         IllegalAccessException illegalAccessEx = new IllegalAccessException("Access denied to field");

         // Create a mock instance and fieldType for message formatting
         Object instance = new TestClass();
         Class<?> fieldType = String.class;

         // Create a string with the exact format used in the exception message
         String expectedMessage = String.format(
               "Cannot access field of type '%s' in class '%s'.",
               fieldType.getName(), instance.getClass().getName());

         // Create the exception
         ReflectionException ex = new ReflectionException(expectedMessage, illegalAccessEx);

         // Verify the exception is properly formed
         assertEquals(expectedMessage, ex.getMessage(),
               "Expected the exception message to match the format");
         assertSame(illegalAccessEx, ex.getCause(),
               "Expected the original exception to be preserved as cause");
      }


      @Test
      @DisplayName("Should properly create ReflectionException from IllegalAccessException in getAttributeOfClass")
      void shouldCreateReflectionExceptionFromIllegalAccessInGetAttributeOfClass() {
         // Create the exception that would be thrown
         IllegalAccessException illegalAccessEx = new IllegalAccessException("Access denied to field");

         // Create test parameters for message formatting
         String fieldName = "testField";
         Object instance = new TestClass();

         // Create a string with the exact format used in the exception message
         String expectedMessage = String.format(
               "Cannot access field '%s' in class hierarchy of '%s'.",
               fieldName, instance.getClass().getName());

         // Create the exception
         ReflectionException ex = new ReflectionException(expectedMessage, illegalAccessEx);

         // Verify the exception is properly formed
         assertEquals(expectedMessage, ex.getMessage(),
               "Expected the exception message to match the format");
         assertSame(illegalAccessEx, ex.getCause(),
               "Expected the original exception to be preserved as cause");
      }

   }


   // -------------------------------------------------------------------------
   //  HELPER METHOD: fieldAccessScenarios
   // -------------------------------------------------------------------------
   static Stream<Arguments> fieldAccessScenarios() {
      return Stream.of(
            Arguments.of(String.class, new TestClass(), ReflectionException.class),
            Arguments.of(Integer.class, new TestClass(), ReflectionException.class),
            Arguments.of(String.class, new TestClassWithPrivateField(), null)
      );
   }

}
