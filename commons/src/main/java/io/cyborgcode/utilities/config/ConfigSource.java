package io.cyborgcode.utilities.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for specifying a configuration source.
 *
 * <p>This annotation is used to mark configuration interfaces with a logical
 * name that represents their configuration source. It serves as metadata
 * that can be used for documentation, reflection, or dynamic configuration
 * handling.
 *
 * <p>This annotation does not directly influence how configurations are loaded
 * but acts as a marker for tools and frameworks that rely on metadata.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigSource {

   /**
    * Specifies the name of the configuration source.
    *
    * @return The logical name of the configuration source.
    */
   String value();
}
