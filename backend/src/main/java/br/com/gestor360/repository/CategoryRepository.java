package br.com.gestor360.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.gestor360.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
}
