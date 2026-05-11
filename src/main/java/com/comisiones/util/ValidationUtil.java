package com.comisiones.util;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class providing centralised Bean Validation (JSR-380) helpers.
 */
public class ValidationUtil {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    private ValidationUtil() {
        // Utility class – no instances
    }

    /**
     * Validates the given object and returns the full set of constraint violations.
     *
     * @param object the object to validate
     * @param <T>    the type of the object
     * @return set of {@link ConstraintViolation}; empty set means the object is valid
     */
    public static <T> Set<ConstraintViolation<T>> validate(T object) {
        return VALIDATOR.validate(object);
    }

    /**
     * Validates the given object and returns a map of {@code field -> errorMessage}.
     * Returns an empty map when the object is valid.
     *
     * @param object the object to validate
     * @param <T>    the type of the object
     * @return map of field name to error message; empty if valid
     */
    public static <T> Map<String, String> validateWithFields(T object) {
        Set<ConstraintViolation<T>> violations = validate(object);
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<T> v : violations) {
            errors.put(v.getPropertyPath().toString(), v.getMessage());
        }
        return errors;
    }

    /**
     * Returns {@code true} when the object passes all constraints.
     *
     * @param object the object to validate
     * @param <T>    the type of the object
     * @return {@code true} if valid
     */
    public static <T> boolean isValid(T object) {
        return validate(object).isEmpty();
    }

    /**
     * Returns a single human-readable error string (fields separated by "; "),
     * or {@code null} when the object is valid.
     *
     * @param object the object to validate
     * @param <T>    the type of the object
     * @return error string or {@code null}
     */
    public static <T> String getErrorMessages(T object) {
        Set<ConstraintViolation<T>> violations = validate(object);
        if (violations.isEmpty()) {
            return null;
        }
        return violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
    }
}
