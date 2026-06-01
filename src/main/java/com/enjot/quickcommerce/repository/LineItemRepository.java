package com.enjot.quickcommerce.repository;

import com.enjot.quickcommerce.domain.LineItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LineItemRepository extends JpaRepository<LineItem, Long> {

    List<LineItem> findByCartId(Long cartId);

    Optional<LineItem> findByIdAndCartId(Long id, Long cartId);
}
