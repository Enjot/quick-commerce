package com.enjot.quickcommerce.exception;

/**
 * Raised when a product cannot be purchased by a given customer
 * (e.g. age restriction not met, perishable item past its sell-by date).
 */
public class ProductNotPurchasableException extends DomainException {

    public ProductNotPurchasableException(String message) {
        super(message);
    }
}
