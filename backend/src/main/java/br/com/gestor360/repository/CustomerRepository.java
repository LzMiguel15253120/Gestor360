package br.com.gestor360.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.gestor360.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
