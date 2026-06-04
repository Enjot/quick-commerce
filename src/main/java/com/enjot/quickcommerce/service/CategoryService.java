package com.enjot.quickcommerce.service;

import com.enjot.quickcommerce.domain.Category;
import com.enjot.quickcommerce.exception.EntityNotFoundException;
import com.enjot.quickcommerce.exception.ResourceConflictException;
import com.enjot.quickcommerce.repository.CategoryRepository;
import com.enjot.quickcommerce.web.dto.CategoryRequest;
import com.enjot.quickcommerce.web.dto.CategoryResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list() {
        return categories.findAll().stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categories.existsByName(request.name())) {
            throw new ResourceConflictException("Category already exists: " + request.name());
        }
        Category saved = categories.save(new Category(request.name()));
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = require(id);
        if (!category.getName().equals(request.name()) && categories.existsByName(request.name())) {
            throw new ResourceConflictException("Category already exists: " + request.name());
        }
        category.setName(request.name());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        categories.delete(require(id));
    }

    @Transactional(readOnly = true)
    public Category require(Long id) {
        return categories.findById(id).orElseThrow(() -> EntityNotFoundException.of("Category", id));
    }
}
