package com.athena.common.aspect;

import com.athena.common.annotation.Dict;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.ValueSerializerModifier;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class DictSerializerModifier extends ValueSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config, BeanDescription.Supplier beanDescSupplier, List<BeanPropertyWriter> beanProperties) {
        BeanDescription beanDesc = beanDescSupplier.get();
        Class<?> beanClass = beanDesc.getType().getRawClass();

        for (BeanPropertyWriter writer : beanProperties) {
            String propertyName = writer.getName();
            Dict dict = findDictAnnotation(beanClass, propertyName);
            if (dict != null) {
                JavaType valueType = writer.getType();
                writer.assignSerializer(new DictSerializer(dict, propertyName, writer.getSerializer(), valueType));
            }
        }

        return beanProperties;
    }

    private Dict findDictAnnotation(Class<?> clazz, String propertyName) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            try {
                Field field = current.getDeclaredField(propertyName);
                Dict dict = field.getAnnotation(Dict.class);
                if (dict != null) {
                    return dict;
                }
            } catch (NoSuchFieldException e) {
                // continue to superclass
            }
            current = current.getSuperclass();
        }
        return null;
    }
}