package annots;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * @author Jenda Kolena, kolena@avast.com
 */
@SuppressWarnings({"unused", "ClassExplicitlyAnnotation"})
public class StoragePathImpl implements StoragePath, Serializable {

    private final String name;

    public StoragePathImpl(final String name) {
        this.name = name;
    }

    @Override
    public String value() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final StoragePathImpl that = (StoragePathImpl) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        // This is specified in java.lang.Annotation.
        return (127 * "value".hashCode()) ^ name.hashCode();
    }

    @Override
    public String toString() {
        return "StoragePath {" + name + "}";
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ConfigProperty.class;
    }

    private static final long serialVersionUID = 0;
}
