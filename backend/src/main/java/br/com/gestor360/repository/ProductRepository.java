package br.com.gestor360.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import br.com.gestor360.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByNameAsc();
    boolean existsByCodeIgnoreCase(String code);
    boolean existsByCategoryId(Long categoryId);
    boolean existsByIdAndActiveTrue(Long id);
    List<Product> findByActiveTrueAndStockLessThanEqual(int threshold);
    @Query("select count(p) from Product p where p.active = true and p.stock <= p.minimumStock")
    long countActiveLowStock();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.stock = p.stock - :quantity where p.id = :id and p.active = true and p.stock >= :quantity")
    int reduceStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Product p set p.stock = p.stock + :quantity where p.id = :id")
    int increaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
