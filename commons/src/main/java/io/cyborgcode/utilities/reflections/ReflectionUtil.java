package io.cyborgcode.utilities.reflections;

import io.cyborgcode.utilities.reflections.exceptions.ReflectionException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

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

   /**
    * Base configuration template:
    * <ul>
    *    <li>Uses the thread context class loader (TCCL) by default.</li>
    *    <li>Scans everything reachable from that class loader.</li>
    *    <li>Registers SubTypes, TypesAnnotated, MethodsAnnotated, and FieldsAnnotated scanners.</li>
    * </ul>
    * Can be overridden from outside (e.g. in a Maven Mojo) with a custom {@link ConfigurationBuilder}.
    */
   private static final AtomicReference<ConfigurationBuilder> BASE_CONFIGURATION_BUILDER =
         new AtomicReference<>(createDefaultConfiguration());

   private ReflectionUtil() {
   }

   // -------------------------------------------------------------------------
   // Base configuration setup / override
   // -------------------------------------------------------------------------

   /**
    * Overrides the base {@link ConfigurationBuilder} used as a template for all scans.
    *
    * <p>Typical usage from a Maven plugin:
    * <ul>
    *    <li>Build a {@code ConfigurationBuilder} with the project classloader(s).</li>
    *    <li>Call this method once at startup.</li>
    * </ul>
    *
    * @param baseConfigurationBuilder configuration to use as the new base template (must not be {@code null})
    * @throws IllegalArgumentException if {@code baseConfigurationBuilder} is {@code null}
    */
   public static void setBaseConfigurationBuilder(ConfigurationBuilder baseConfigurationBuilder) {
      if (baseConfigurationBuilder == null) {
         throw new IllegalArgumentException("ConfigurationBuilder cannot be null.");
      }
      BASE_CONFIGURATION_BUILDER.set(baseConfigurationBuilder);
   }

   /**
    * Returns the current base {@link ConfigurationBuilder} reference.
    *
    * <p>Note: the returned builder is shared; callers should treat it as read-only and
    * prefer to copy its settings instead of mutating it directly.
    *
    * @return the current base configuration builder
    */
   public static ConfigurationBuilder getBaseConfigurationBuilder() {
      return BASE_CONFIGURATION_BUILDER.get();
   }

   /**
    * Creates the default {@link ConfigurationBuilder} used when the user does not override it.
    * <ul>
    *    <li>Uses the thread context class loader (TCCL).</li>
    *    <li>Registers all relevant scanners.</li>
    *    <li>Adds URLs for the entire classpath of that loader.</li>
    * </ul>
    *
    * @return a new default configuration builder
    */
   private static ConfigurationBuilder createDefaultConfiguration() {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();

      ConfigurationBuilder builder = new ConfigurationBuilder()
            .addClassLoaders(cl != null ? new ClassLoader[] {cl} : new ClassLoader[0])
            .addUrls(ClasspathHelper.forClassLoader(cl))
            .setScanners(
                  Scanners.SubTypes,
                  Scanners.TypesAnnotated,
                  Scanners.MethodsAnnotated,
                  Scanners.FieldsAnnotated
            )
            .addUrls(urlsFromClassLoader(cl));

      if (cl != null) {
         builder.addUrls(ClasspathHelper.forClassLoader(cl));
      }

      return builder;
   }

   /**
    * Collects classpath URLs for the given class loader, falling back to the full
    * {@code java.class.path} if no URLs are found.
    *
    * <p>Duplicate URLs are removed using their external form.
    *
    * @param cl class loader to inspect (may be {@code null})
    * @return a de-duplicated list of URLs to scan
    */
   private static List<URL> urlsFromClassLoader(ClassLoader cl) {
      List<URL> result = new ArrayList<>();
      Set<String> seen = new LinkedHashSet<>();

      for (URL url : ClasspathHelper.forClassLoader(cl)) {
         addIfNotSeen(url, seen, result);
      }

      if (result.isEmpty()) {
         for (URL url : ClasspathHelper.forJavaClassPath()) {
            addIfNotSeen(url, seen, result);
         }
      }

      return result;
   }

   /**
    * Adds the URL to the result list if it has not been seen yet, using the URL's
    * external form for equality.
    *
    * @param url   URL candidate
    * @param seen  set of already processed URL keys
    * @param result target list for unique URLs
    */
   private static void addIfNotSeen(URL url, Set<String> seen, List<URL> result) {
      // Use external form (or URI) to deduplicate, *not* URL.equals/hashCode
      String key = url.toExternalForm();
      if (seen.add(key)) {
         result.add(url);
      }
   }

   // -------------------------------------------------------------------------
   // Public API (with String... packagePrefixes)
   // -------------------------------------------------------------------------

   /**
    * Finds all enum classes that implement a given interface.
    *
    * <p>If no {@code packagePrefixes} are provided, this scans everything reachable
    * from the base configuration. If prefixes are provided, scanning is restricted
    * to those packages.
    *
    * @param interfaceClass   interface to search implementations for (must not be {@code null})
    * @param packagePrefixes  optional package prefixes used to limit scanning
    * @param <T>              type of the interface
    * @return list of enum classes implementing the interface
    * @throws IllegalArgumentException if {@code interfaceClass} is {@code null}
    * @throws ReflectionException      if no matching enum implementations are found
    */
   @SuppressWarnings("unchecked")
   public static <T> List<Class<? extends Enum>> findEnumClassImplementationsOfInterface(
         Class<T> interfaceClass, String... packagePrefixes) {

      validateInputs(interfaceClass);

      Reflections reflections = createReflections(packagePrefixes);
      Set<Class<? extends T>> result = reflections.getSubTypesOf(interfaceClass);

      List<Class<? extends Enum>> listOfEnumClasses = new ArrayList<>();
      for (Class<? extends T> cls : result) {
         if (cls.isEnum()) {
            Class<? extends Enum<?>> enumClass = (Class<? extends Enum<?>>) cls;
            listOfEnumClasses.add(enumClass);
         }
      }

      if (listOfEnumClasses.isEmpty()) {
         throw new ReflectionException(String.format(
               "No Enum implementing interface '%s' found in packages '%s'.",
               interfaceClass.getName(), Arrays.toString(packagePrefixes)));
      }
      return listOfEnumClasses;
   }

   /**
    * Finds a specific enum constant that implements a given interface.
    *
    * <p>All enum classes implementing the interface (within the optional package
    * prefixes) are scanned and the enum value with the given {@code enumName}
    * is returned.
    *
    * @param interfaceClass   interface implemented by the enum constants
    * @param enumName         name of the enum constant to find
    * @param packagePrefixes  optional package prefixes used to limit scanning
    * @param <T>              interface type
    * @return the matching enum constant implementing the interface
    * @throws IllegalArgumentException if {@code interfaceClass} or {@code enumName} is {@code null} or empty
    * @throws ReflectionException      if the value is not found or found more than once
    */
   @SuppressWarnings("unchecked")
   public static <T> T findEnumImplementationsOfInterface(
         Class<T> interfaceClass, String enumName, String... packagePrefixes) {

      List<Class<? extends Enum>> enumClassImplementationsOfInterface =
            findEnumClassImplementationsOfInterface(interfaceClass, packagePrefixes);

      List<? extends Enum> enumValuesList = enumClassImplementationsOfInterface.stream()
            .flatMap(enumClass -> Arrays.stream(enumClass.getEnumConstants()))
            .filter(anEnum -> anEnum.name().equals(enumName))
            .toList();

      if (enumValuesList.isEmpty()) {
         throw new ReflectionException(String.format(
               "Enum value '%s' not found in any enum class for packages '%s'.",
               enumName, Arrays.toString(packagePrefixes)));
      }
      if (enumValuesList.size() > 1) {
         throw new ReflectionException(String.format(
               "There are more than one enum with value '%s'.", enumName));
      }
      return (T) enumValuesList.get(0);
   }

   /**
    * Finds all class implementations of a given interface.
    *
    * <p>If no {@code packagePrefixes} are provided, this scans everything reachable
    * from the base configuration. If prefixes are provided, scanning is restricted
    * to those packages.
    *
    * @param interfaceClass   interface to search implementations for (must not be {@code null})
    * @param packagePrefixes  optional package prefixes used to limit scanning
    * @param <T>              type of the interface
    * @return list of classes implementing the interface (possibly empty)
    * @throws IllegalArgumentException if {@code interfaceClass} is {@code null}
    */
   public static <T> List<Class<? extends T>> findImplementationsOfInterface(
         Class<T> interfaceClass, String... packagePrefixes) {

      validateInputs(interfaceClass);

      Reflections reflections = createReflections(packagePrefixes);
      Set<Class<? extends T>> result = reflections.getSubTypesOf(interfaceClass);
      return new ArrayList<>(result);
   }

   /**
    * Retrieves all field values of a specified type from an object, including fields
    * declared in superclasses.
    *
    * <p>Only fields whose type is assignable to {@code fieldType} and whose runtime
    * value is an instance of {@code fieldType} are collected.
    *
    * @param instance  object to inspect (must not be {@code null})
    * @param fieldType type of fields to extract (must not be {@code null})
    * @param <K>       target field type
    * @return list of matching field values
    * @throws IllegalArgumentException if inputs are {@code null} or {@code fieldType} is empty
    * @throws ReflectionException      if no matching fields are found or field access fails
    */
   @SuppressWarnings("java:S3011")
   public static <K> List<K> getFieldValues(Object instance, Class<K> fieldType) {
      validateInputs(instance, fieldType);

      try {
         Class<?> currentClass = instance.getClass();
         List<Field> allFields = new ArrayList<>();

         while (currentClass != null && currentClass != Object.class) {
            allFields.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
         }

         List<K> matchingValues = new ArrayList<>();

         for (Field field : allFields) {
            field.setAccessible(true);

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
    * Builds a {@link Reflections} instance using the current base configuration
    * and optional package filters.
    *
    * <p>When no {@code packagePrefixes} are provided, the base configuration is used
    * as-is. When prefixes are provided, a new configuration is created by copying
    * the base settings and applying an additional package filter.
    *
    * @param packagePrefixes optional package prefixes used to limit scanning
    * @return a configured {@link Reflections} instance ready for scanning
    */
   @SuppressWarnings("java:S2112") // Reflections API requires URL collections; no network access or URL keys here
   private static Reflections createReflections(String... packagePrefixes) {
      if (packagePrefixes == null || packagePrefixes.length == 0) {
         return new Reflections(getBaseConfigurationBuilder());
      }

      ConfigurationBuilder base = getBaseConfigurationBuilder();
      ConfigurationBuilder builder = new ConfigurationBuilder();

      ClassLoader[] cls = base.getClassLoaders();
      if (cls != null && cls.length > 0) {
         builder.addClassLoaders(cls);
      }

      Set<Scanner> scanners = base.getScanners();
      if (scanners != null && !scanners.isEmpty()) {
         builder.setScanners(scanners.toArray(new Scanner[0]));
      }

      Set<URL> urls = base.getUrls();
      if (urls != null && !urls.isEmpty()) {
         builder.setUrls(urls);
      }

      builder.setParallel(base.isParallel());
      builder.setExpandSuperTypes(base.shouldExpandSuperTypes());

      Predicate<String> baseFilter = base.getInputsFilter(); // default: accept-all
      FilterBuilder pkgFilter = new FilterBuilder();
      for (String pkg : packagePrefixes) {
         pkgFilter.includePackage(pkg);
      }
      builder.filterInputsBy(baseFilter.and(pkgFilter));

      ClassLoader[] loaders = cls != null ? cls : new ClassLoader[0];
      for (String pkg : packagePrefixes) {
         builder.forPackage(pkg, loaders);
      }

      return new Reflections(builder);
   }

   /**
    * Validates that the given objects are non-null and that any {@link String}
    * values are not empty or blank.
    *
    * @param objects input parameters to validate
    * @throws IllegalArgumentException if any object is {@code null} or any {@code String} is empty/blank
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
