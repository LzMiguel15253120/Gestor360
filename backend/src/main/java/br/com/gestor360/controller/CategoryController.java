package br.com.gestor360.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.gestor360.dto.CatalogDtos.*;
import br.com.gestor360.service.CatalogService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
public class CategoryController {
    private final CatalogService service;
    public CategoryController(CatalogService service) { this.service = service; }
    @GetMapping public List<CategoryView> list() { return service.categories(); }
    @PostMapping public ResponseEntity<CategoryView> create(@Valid @RequestBody CategoryRequest request) {
        CategoryView created = service.createCategory(request);
        return ResponseEntity.created(URI.create("/api/categorias/" + created.id())).body(created);
    }
    @PutMapping("/{id}") public CategoryView update(@PathVariable long id, @Valid @RequestBody CategoryRequest request) { return service.updateCategory(id, request); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable long id) { service.deleteCategory(id); return ResponseEntity.noContent().build(); }
}
