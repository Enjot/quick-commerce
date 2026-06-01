package com.enjot.quickcommerce.repository;

import com.enjot.quickcommerce.domain.AgeRestrictedProduct;
import com.enjot.quickcommerce.domain.Category;
import com.enjot.quickcommerce.domain.PerishableProduct;
import com.enjot.quickcommerce.domain.Product;
import com.enjot.quickcommerce.domain.ProductType;
import com.enjot.quickcommerce.domain.RegularProduct;
import com.enjot.quickcommerce.support.PostgresContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest extends PostgresContainerSupport {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ProductRepository products;

    private Category persistCategory() {
        return em.persistFlushFind(new Category("Groceries"));
    }

    private <T extends Product> T configure(T product, Category category, String sku, String name, boolean active) {
        product.setCategory(category);
        product.setSku(sku);
        product.setName(name);
        product.setPrice(new BigDecimal("9.99"));
        product.setStockQuantity(10);
        product.setActive(active);
        return product;
    }

    @Test
    void persistsAndRetrievesEachConcreteSubtypePolymorphically() {
        Category category = persistCategory();

        RegularProduct regular = configure(new RegularProduct(), category, "REG-1", "Bread", true);

        PerishableProduct perishable = configure(new PerishableProduct(), category, "PER-1", "Milk", true);
        perishable.setExpiryDate(LocalDate.now().plusDays(3));
        perishable.setShelfLifeDays(5);

        AgeRestrictedProduct restricted = configure(new AgeRestrictedProduct(), category, "AGE-1", "Wine", true);
        restricted.setMinimumAge(18);

        products.saveAll(List.of(regular, perishable, restricted));
        em.flush();
        em.clear();

        Product loadedRegular = products.findBySku("REG-1").orElseThrow();
        Product loadedPerishable = products.findBySku("PER-1").orElseThrow();
        Product loadedRestricted = products.findBySku("AGE-1").orElseThrow();

        assertThat(loadedRegular).isInstanceOf(RegularProduct.class);
        assertThat(loadedRegular.getType()).isEqualTo(ProductType.REGULAR);

        assertThat(loadedPerishable).isInstanceOf(PerishableProduct.class);
        assertThat(((PerishableProduct) loadedPerishable).getShelfLifeDays()).isEqualTo(5);

        assertThat(loadedRestricted).isInstanceOf(AgeRestrictedProduct.class);
        assertThat(((AgeRestrictedProduct) loadedRestricted).getMinimumAge()).isEqualTo(18);
    }

    @Test
    void findByActiveTrueExcludesInactiveProducts() {
        Category category = persistCategory();
        products.save(configure(new RegularProduct(), category, "REG-A", "Active", true));
        products.save(configure(new RegularProduct(), category, "REG-B", "Hidden", false));
        em.flush();

        List<Product> active = products.findByActiveTrue();

        assertThat(active).extracting(Product::getSku).containsExactly("REG-A");
    }

    @Test
    void searchActiveMatchesByNameCaseInsensitively() {
        Category category = persistCategory();
        products.save(configure(new RegularProduct(), category, "REG-C", "Organic Banana", true));
        products.save(configure(new RegularProduct(), category, "REG-D", "Apple", true));
        em.flush();

        assertThat(products.searchActive("banana")).extracting(Product::getSku).containsExactly("REG-C");
        assertThat(products.searchActive(null)).hasSize(2);
    }

    @Test
    void existsBySkuReflectsPersistedState() {
        Category category = persistCategory();
        products.save(configure(new RegularProduct(), category, "REG-E", "Salt", true));
        em.flush();

        assertThat(products.existsBySku("REG-E")).isTrue();
        assertThat(products.existsBySku("MISSING")).isFalse();
    }
}
