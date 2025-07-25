package io.github.rosestack.core.spring;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public abstract class FieldUtils {
    public static <T> T getFieldValue(Object object, String fieldName) {
        return (T) getFieldValue(object, fieldName, (Class) null);
    }

    public static <T> T getFieldValue(Object object, String fieldName, T defaultValue) {
        T value = (T) getFieldValue(object, fieldName);
        return (T) (value != null ? value : defaultValue);
    }

    public static <T> T getFieldValue(Object object, String fieldName, Class<T> fieldType) {
        T fieldValue = null;
        Field field = ReflectionUtils.findField(object.getClass(), fieldName, fieldType);
        if (field != null) {
            boolean accessible = field.isAccessible();

            try {
                if (!accessible) {
                    ReflectionUtils.makeAccessible(field);
                }

                fieldValue = (T) ReflectionUtils.getField(field, object);
            } finally {
                if (!accessible) {
                    field.setAccessible(accessible);
                }

            }
        }

        return fieldValue;
    }
}