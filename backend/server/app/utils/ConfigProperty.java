package utils;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Jenda Kolena, kolena@avast.com
 */
@Qualifier
@Documented
@Retention(RUNTIME)
@SuppressWarnings("unused")
public @interface ConfigProperty {

    /**
     * The name.
     */
    String value();
}
