package annots;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Jenda Kolena, kolena@avast.com
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface CallbackExecutor {
}
