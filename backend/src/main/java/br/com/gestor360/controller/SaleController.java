package br.com.gestor360.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.gestor360.dto.OperationDtos.SaleRequest;
import br.com.gestor360.dto.OperationDtos.SaleView;
import br.com.gestor360.service.OperationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/vendas")
public class SaleController {
    private final OperationService service;
    public SaleController(OperationService service) { this.service = service; }
    @GetMapping public List<SaleView> list() { return service.sales(); }
    @GetMapping("/{id}") public SaleView get(@PathVariable long id) { return service.sale(id); }
    @PostMapping public ResponseEntity<SaleView> create(@Valid @RequestBody SaleRequest request) {
        SaleView created = service.createSale(request);
        return ResponseEntity.created(URI.create("/api/vendas/" + created.id())).body(created);
    }
    @PostMapping("/{id}/cancelar") public SaleView cancel(@PathVariable long id) { return service.cancelSale(id); }
}
