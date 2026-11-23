package br.com.gestor360.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.gestor360.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findTop10ByOrderByCreatedAtDesc();
    List<Sale> findAllByOrderByCreatedAtDesc();
    List<Sale> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<Sale> findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(Instant from, Instant to);
    boolean existsByCustomerId(Long customerId);
}
