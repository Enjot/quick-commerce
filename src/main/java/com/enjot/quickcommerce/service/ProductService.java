package com.enjot.quickcommerce.service;

import com.enjot.quickcommerce.domain.Category;
import com.enjot.quickcommerce.domain.Product;
import com.enjot.quickcommerce.exception.EntityNotFoundException;
import com.enjot.quickcommerce.exception.ResourceConflictException;
import com.enjot.quickcommerce.repository.ProductRepository;
import com.enjot.quickcommerce.web.dto.ProductRequest;
import com.enjot.quickcommerce.web.dto.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository products;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository products, CategoryService categoryService, ProductMapper productMapper) {
        this.products = products;
        this.categoryService = categoryService;
        this.productMapper = productMapper;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> browse(String search) {
        List<Product> result = StringUtils.hasText(search)
                ? products.searchActive(search)
                : products.findByActiveTrue();
        return result.stream().map(ProductResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return ProductResponse.from(require(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (products.existsBySku(request.sku())) {
            throw new ResourceConflictException("Product SKU already exists: " + request.sku());
        }
        Category category = categoryService.require(request.categoryId());
        Product product = productMapper.toNewEntity(request, category);
        return ProductResponse.from(products.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = require(id);
        if (!product.getSku().equals(request.sku()) && products.existsBySku(request.sku())) {
            throw new ResourceConflictException("Product SKU already exists: " + request.sku());
        }
        Category category = categoryService.require(request.categoryId());
        productMapper.applyUpdate(product, request, category);
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long id) {
        products.delete(require(id));
    }

    private Product require(Long id) {
        return products.findById(id).orElseThrow(() -> EntityNotFoundException.of("Product", id));
    }
}
