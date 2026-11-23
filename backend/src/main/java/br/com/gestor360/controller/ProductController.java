package br.com.gestor360.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.gestor360.dto.CatalogDtos.*;
import br.com.gestor360.service.CatalogService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/produtos")
public class ProductController {
    private final CatalogService service;
    public ProductController(CatalogService service) { this.service = service; }
    @GetMapping public List<ProductView> list() { return service.products(); }
    @GetMapping("/{id}") public ProductView get(@PathVariable long id) { return service.product(id); }
    @PostMapping public ResponseEntity<ProductView> create(@Valid @RequestBody ProductRequest request) {
        ProductView created = service.createProduct(request);
        return ResponseEntity.created(URI.create("/api/produtos/" + created.id())).body(created);
    }
    @PutMapping("/{id}") public ProductView update(@PathVariable long id, @Valid @RequestBody ProductRequest request) { return service.updateProduct(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> deactivate(@PathVariable long id) { service.deactivateProduct(id); return ResponseEntity.noContent().build(); }
    @PostMapping("/{id}/ativar") public ResponseEntity<Void> activate(@PathVariable long id) { service.activateProduct(id); return ResponseEntity.noContent().build(); }
}
