package com.example.carins.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.EmbeddedId;
import org.junit.jupiter.api.Test;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PojoReflectionTest {

    private static final List<Class<?>> MODEL_CLASSES = List.of(
            Car.class,
            InsurancePolicy.class,
            InsuranceClaim.class,
            Owner.class
    );

    @Test
    void testAllModelClasses() {
        for (Class<?> clazz : MODEL_CLASSES) {
            testPojoClass(clazz);
        }
    }

    private void testPojoClass(Class<?> clazz) {
        Object obj;
        try {
            Constructor<?> ctor = clazz.getDeclaredConstructor();
            ctor.setAccessible(true);
            obj = ctor.newInstance();
        } catch (Exception e) {
            return;
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) continue;

            if (field.isAnnotationPresent(Id.class) ||
                    field.isAnnotationPresent(EmbeddedId.class) ||
                    field.isAnnotationPresent(GeneratedValue.class)) {
                continue;
            }

            String name = field.getName();
            Class<?> type = field.getType();
            String capital = Character.toUpperCase(name.charAt(0)) + name.substring(1);

            String getterName = (type == boolean.class || type == Boolean.class)
                    ? "is" + capital
                    : "get" + capital;
            String setterName = "set" + capital;

            Method getter;
            try {
                getter = clazz.getMethod(getterName);
            } catch (NoSuchMethodException e) {
                continue;
            }

            Method setter;
            try {
                setter = clazz.getMethod(setterName, getter.getReturnType());
            } catch (NoSuchMethodException e) {
                continue;
            }

            Object value = sampleValueFor(type);
            if (value == null) continue;

            try {
                setter.invoke(obj, value);
                Object read = getter.invoke(obj);
                assertEquals(value, read,
                        () -> "Getter should return value set by setter for " + clazz.getSimpleName() + "." + name);
            } catch (Exception e) {
                fail("Failed invoking getter/setter for " + clazz.getSimpleName() + "." + name + ": " + e);
            }
        }
    }

    private Object sampleValueFor(Class<?> type) {
        if (type == String.class) return "str";
        if (type == Long.class || type == long.class) return 1L;
        if (type == Integer.class || type == int.class) return 1;
        if (type == Double.class || type == double.class) return 1.0;
        if (type == Float.class || type == float.class) return 1.0f;
        if (type == Boolean.class || type == boolean.class) return Boolean.TRUE;
        if (type == Date.class) return new Date();
        if (type == BigDecimal.class) return new BigDecimal("1.00");
        if (type == LocalDate.class) return LocalDate.now();
        if (type == LocalDateTime.class) return LocalDateTime.now();
        if (type == Car.class) return new Car();
        if (type == Owner.class) return new Owner();
        return null;
    }
}
