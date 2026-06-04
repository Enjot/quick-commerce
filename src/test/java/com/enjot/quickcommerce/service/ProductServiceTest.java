package com.enjot.quickcommerce.service;

import com.enjot.quickcommerce.domain.Category;
import com.enjot.quickcommerce.domain.Product;
import com.enjot.quickcommerce.domain.ProductType;
import com.enjot.quickcommerce.domain.RegularProduct;
import com.enjot.quickcommerce.exception.EntityNotFoundException;
import com.enjot.quickcommerce.exception.ResourceConflictException;
import com.enjot.quickcommerce.repository.ProductRepository;
import com.enjot.quickcommerce.web.dto.ProductRequest;
import com.enjot.quickcommerce.web.dto.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository products;

    @Mock
    private CategoryService categoryService;

    private ProductService service;

    @BeforeEach
    void setUp() {
        service = new ProductService(products, categoryService, new ProductMapper());
    }

    private Category category() {
        Category category = new Category("Groceries");
        category.setId(1L);
        return category;
    }

    private ProductRequest regularRequest() {
        return new ProductRequest(ProductType.REGULAR, "SKU-1", "Bread", "Fresh",
                new BigDecimal("3.50"), 1L, 20, true, null, null, null);
    }

    @Test
    void createPersistsProductAndReturnsResponse() {
        given(products.existsBySku("SKU-1")).willReturn(false);
        given(categoryService.require(1L)).willReturn(category());
        given(products.save(any(Product.class))).willAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProductResponse response = service.create(regularRequest());

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.sku()).isEqualTo("SKU-1");
        assertThat(response.type()).isEqualTo(ProductType.REGULAR);
        assertThat(response.categoryName()).isEqualTo("Groceries");
    }

    @Test
    void createRejectsDuplicateSku() {
        given(products.existsBySku("SKU-1")).willReturn(true);

        assertThatThrownBy(() -> service.create(regularRequest()))
                .isInstanceOf(ResourceConflictException.class);
        verify(products, never()).save(any());
    }

    @Test
    void createFailsWhenCategoryMissing() {
        given(products.existsBySku("SKU-1")).willReturn(false);
        given(categoryService.require(1L)).willThrow(EntityNotFoundException.of("Category", 1L));

        assertThatThrownBy(() -> service.create(regularRequest()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void createAgeRestrictedRequiresMinimumAge() {
        given(products.existsBySku("SKU-2")).willReturn(false);
        given(categoryService.require(1L)).willReturn(category());
        ProductRequest request = new ProductRequest(ProductType.AGE_RESTRICTED, "SKU-2", "Wine", null,
                new BigDecimal("19.99"), 1L, 5, true, null, null, null);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateRejectsTypeChange() {
        RegularProduct existing = new RegularProduct();
        existing.setId(7L);
        existing.setSku("SKU-1");
        given(products.findById(7L)).willReturn(java.util.Optional.of(existing));
        given(categoryService.require(1L)).willReturn(category());
        ProductRequest perishable = new ProductRequest(ProductType.PERISHABLE, "SKU-1", "Bread", null,
                new BigDecimal("3.50"), 1L, 20, true, LocalDate.now().plusDays(2), 3, null);

        assertThatThrownBy(() -> service.update(7L, perishable))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void browseUsesSearchOrActiveListing() {
        given(products.findByActiveTrue()).willReturn(List.of());
        given(products.searchActive("milk")).willReturn(List.of());

        service.browse("  ");
        service.browse("milk");

        verify(products).findByActiveTrue();
        verify(products).searchActive("milk");
    }

    @Test
    void getMissingProductThrows() {
        given(products.findById(99L)).willReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> service.get(99L)).isInstanceOf(EntityNotFoundException.class);
    }
}
