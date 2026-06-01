package com.enjot.quickcommerce.repository;

import com.enjot.quickcommerce.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    List<Product> findByActiveTrue();

    @Query("""
            select p from Product p
            where p.active = true
              and (:term is null or lower(p.name) like lower(concat('%', :term, '%')))
            """)
    List<Product> searchActive(@Param("term") String term);
}
