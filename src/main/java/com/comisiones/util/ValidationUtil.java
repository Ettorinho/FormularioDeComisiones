package com.comisiones.util;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for Bean Validation (JSR-380) using Hibernate Validator.
 * Provides centralized, reusable validation for model objects.
 */
public class ValidationUtil {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    private ValidationUtil() {
        // Utility class; do not instantiate
    }

    /**
     * Validates the given object and returns a list of error messages.
     *
     * @param <T>    the type of the object to validate
     * @param object the object to validate
     * @return a list of validation error messages, empty if valid
     */
    public static <T> List<String> validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        List<String> errors = new ArrayList<>();
        for (ConstraintViolation<T> violation : violations) {
            errors.add(violation.getMessage());
        }
        return errors;
    }

    /**
     * Validates the given object and returns a map of field names to error messages.
     *
     * @param <T>    the type of the object to validate
     * @param object the object to validate
     * @return a map where each key is a field name and the value is the error message
     */
    public static <T> Map<String, String> validateWithFields(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<T> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            errors.put(fieldName, violation.getMessage());
        }
        return errors;
    }

    /**
     * Returns {@code true} if the given object passes all validation constraints.
     *
     * @param <T>    the type of the object to validate
     * @param object the object to validate
     * @return {@code true} if valid, {@code false} otherwise
     */
    public static <T> boolean isValid(T object) {
        return validator.validate(object).isEmpty();
    }
}
