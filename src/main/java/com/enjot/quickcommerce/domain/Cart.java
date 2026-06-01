package com.enjot.quickcommerce.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A single active shopping cart per user.
 */
@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineItem> lineItems = new ArrayList<>();

    public Cart(User user) {
        this.user = user;
    }

    public void addLineItem(LineItem item) {
        item.setCart(this);
        lineItems.add(item);
    }

    public void removeLineItem(LineItem item) {
        lineItems.remove(item);
        item.setCart(null);
    }

    /**
     * @return live total computed from the current line-item unit prices.
     */
    public BigDecimal total() {
        return lineItems.stream()
                .map(LineItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
