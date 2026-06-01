package com.enjot.quickcommerce.domain;

/**
 * Discriminator for the {@link Product} hierarchy.
 */
public enum ProductType {
    REGULAR,
    PERISHABLE,
    AGE_RESTRICTED
}
