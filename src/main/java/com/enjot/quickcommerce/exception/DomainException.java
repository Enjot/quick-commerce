package com.enjot.quickcommerce.exception;

/**
 * Base type for business-rule violations originating in the domain layer.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
