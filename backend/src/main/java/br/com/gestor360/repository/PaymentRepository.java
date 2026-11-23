package br.com.gestor360.repository;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.gestor360.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByPaidAtGreaterThanEqualAndPaidAtLessThan(Instant from, Instant to);
    List<Payment> findByAccountIdOrderByPaidAtDesc(Long accountId);
}
