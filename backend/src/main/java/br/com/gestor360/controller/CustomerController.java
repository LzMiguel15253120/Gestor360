package br.com.gestor360.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.gestor360.dto.CatalogDtos.*;
import br.com.gestor360.dto.OperationDtos.SaleView;
import br.com.gestor360.service.CatalogService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
public class CustomerController {
    private final CatalogService service;
    public CustomerController(CatalogService service) { this.service = service; }
    @GetMapping public List<PartyView> list() { return service.customers(); }
    @GetMapping("/{id}/compras") public List<SaleView> history(@PathVariable long id) { return service.customerHistory(id); }
    @PostMapping public ResponseEntity<PartyView> create(@Valid @RequestBody PartyRequest request) {
        PartyView created = service.createCustomer(request);
        return ResponseEntity.created(URI.create("/api/clientes/" + created.id())).body(created);
    }
    @PutMapping("/{id}") public PartyView update(@PathVariable long id, @Valid @RequestBody PartyRequest request) { return service.updateCustomer(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable long id) { service.deleteCustomer(id); return ResponseEntity.noContent().build(); }
}
