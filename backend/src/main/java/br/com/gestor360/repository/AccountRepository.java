package br.com.gestor360.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import br.com.gestor360.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findAllByOrderByDueDateAsc();
    List<Account> findByTypeAndStatusNot(String type, String status);
    java.util.Optional<Account> findBySaleId(Long saleId);
}
