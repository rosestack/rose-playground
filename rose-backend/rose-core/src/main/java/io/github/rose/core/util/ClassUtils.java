package io.github.rose.core.util;

/**
 * TODO Comment
 *
 * @author <a href="mailto:ichensoul@gmail.com">chensoul</a>
 * @since TODO
 */
public class ClassUtils {
    public static <T> T cast(Object object, Class<T> castType) {
        if (object == null || castType == null) {
            return null;
        }
        return castType.isInstance(object) ? castType.cast(object) : null;
    }
}
