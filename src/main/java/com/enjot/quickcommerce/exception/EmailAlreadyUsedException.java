package com.enjot.quickcommerce.exception;

/**
 * Raised when registration is attempted with an email that already exists.
 */
public class EmailAlreadyUsedException extends DomainException {

    public EmailAlreadyUsedException(String email) {
        super("Email already in use: " + email);
    }
}
