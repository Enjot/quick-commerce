package com.enjot.quickcommerce.repository;

import com.enjot.quickcommerce.domain.Category;
import com.enjot.quickcommerce.support.PostgresContainerSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest extends PostgresContainerSupport {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CategoryRepository categories;

    @Test
    void findByNameAndExistsByNameWorkOnPersistedCategory() {
        em.persistFlushFind(new Category("Beverages"));

        assertThat(categories.findByName("Beverages")).isPresent();
        assertThat(categories.existsByName("Beverages")).isTrue();
        assertThat(categories.existsByName("Unknown")).isFalse();
    }
}
