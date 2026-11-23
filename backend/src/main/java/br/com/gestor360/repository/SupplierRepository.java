package br.com.gestor360.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.com.gestor360.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> { }
