package br.com.gestor360.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.gestor360.entity.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findTop100ByOrderByCreatedAtDesc();
    boolean existsByProductIdAndReferenceTypeAndReferenceIdAndType(Long productId, String referenceType, Long referenceId, String type);
}
