package annots;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface CallbackExecutor {
}
