package com.enjot.quickcommerce.exception;

/**
 * Raised when a referenced entity cannot be found.
 */
public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public static EntityNotFoundException of(String entity, Object id) {
        return new EntityNotFoundException("%s not found: %s".formatted(entity, id));
    }
}
