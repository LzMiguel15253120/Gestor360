package br.com.gestor360.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.gestor360.dto.CatalogDtos.*;
import br.com.gestor360.service.CatalogService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/fornecedores")
public class SupplierController {
    private final CatalogService service;
    public SupplierController(CatalogService service) { this.service = service; }
    @GetMapping public List<PartyView> list() { return service.suppliers(); }
    @PostMapping public ResponseEntity<PartyView> create(@Valid @RequestBody PartyRequest request) {
        PartyView created = service.createSupplier(request);
        return ResponseEntity.created(URI.create("/api/fornecedores/" + created.id())).body(created);
    }
    @PutMapping("/{id}") public PartyView update(@PathVariable long id, @Valid @RequestBody PartyRequest request) { return service.updateSupplier(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable long id) { service.deleteSupplier(id); return ResponseEntity.noContent().build(); }
}
