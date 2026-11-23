package br.com.gestor360.repository;

import java.util.List;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.gestor360.entity.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findAllByOrderByCreatedAtDesc();
    boolean existsBySupplierId(Long supplierId);
    java.util.Optional<Purchase> findByIdAndStatus(Long id, String status);
    List<Purchase> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(Instant from, Instant to);
}
