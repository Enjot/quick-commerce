package com.enjot.quickcommerce.domain;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Abstract catalogue product — the polymorphism anchor of the domain.
 * Mapped with {@code SINGLE_TABLE} inheritance; the {@code product_type}
 * discriminator selects the concrete subtype.
 */
@Entity
@Table(name = "products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "product_type")
@Getter
@Setter
public abstract class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Column(nullable = false)
    private boolean active = true;

    /**
     * @return the concrete catalogue type of this product.
     */
    public abstract ProductType getType();

    /**
     * Polymorphic purchase guard invoked for every line item at checkout.
     * Subtypes enforce their own rules; the caller never branches on type.
     *
     * @param customerDateOfBirth ordering customer's date of birth (may be {@code null})
     * @throws com.enjot.quickcommerce.exception.ProductNotPurchasableException when the rule is violated
     */
    public abstract void validateForOrder(LocalDate customerDateOfBirth);
}
