package com.enjot.quickcommerce.service;

import com.enjot.quickcommerce.domain.Category;
import com.enjot.quickcommerce.exception.EntityNotFoundException;
import com.enjot.quickcommerce.exception.ResourceConflictException;
import com.enjot.quickcommerce.repository.CategoryRepository;
import com.enjot.quickcommerce.web.dto.CategoryRequest;
import com.enjot.quickcommerce.web.dto.CategoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categories;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        service = new CategoryService(categories);
    }

    private Category category(long id, String name) {
        Category category = new Category(name);
        category.setId(id);
        return category;
    }

    @Test
    void listMapsAllCategories() {
        given(categories.findAll()).willReturn(List.of(category(1L, "A"), category(2L, "B")));

        assertThat(service.list()).extracting(CategoryResponse::name).containsExactly("A", "B");
    }

    @Test
    void createPersistsWhenNameIsFree() {
        given(categories.existsByName("Bakery")).willReturn(false);
        given(categories.save(any(Category.class))).willAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setId(3L);
            return c;
        });

        CategoryResponse response = service.create(new CategoryRequest("Bakery"));

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.name()).isEqualTo("Bakery");
    }

    @Test
    void createRejectsDuplicateName() {
        given(categories.existsByName("Bakery")).willReturn(true);

        assertThatThrownBy(() -> service.create(new CategoryRequest("Bakery")))
                .isInstanceOf(ResourceConflictException.class);
        verify(categories, never()).save(any());
    }

    @Test
    void updateRenamesCategory() {
        given(categories.findById(1L)).willReturn(Optional.of(category(1L, "Old")));
        given(categories.existsByName("New")).willReturn(false);

        assertThat(service.update(1L, new CategoryRequest("New")).name()).isEqualTo("New");
    }

    @Test
    void updateRejectsRenameToExistingName() {
        given(categories.findById(1L)).willReturn(Optional.of(category(1L, "Old")));
        given(categories.existsByName("Taken")).willReturn(true);

        assertThatThrownBy(() -> service.update(1L, new CategoryRequest("Taken")))
                .isInstanceOf(ResourceConflictException.class);
    }

    @Test
    void updateMissingCategoryThrows() {
        given(categories.findById(9L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(9L, new CategoryRequest("X")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteRemovesExistingCategory() {
        Category category = category(1L, "Old");
        given(categories.findById(1L)).willReturn(Optional.of(category));

        service.delete(1L);

        verify(categories).delete(category);
    }

    @Test
    void requireMissingCategoryThrows() {
        given(categories.findById(2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.require(2L)).isInstanceOf(EntityNotFoundException.class);
    }
}
