package com.enjot.quickcommerce.exception;

/**
 * Raised when a request conflicts with existing state, e.g. a duplicate
 * unique key such as a product SKU or category name.
 */
public class ResourceConflictException extends DomainException {

    public ResourceConflictException(String message) {
        super(message);
    }
}
