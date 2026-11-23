package br.com.gestor360.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.gestor360.dto.OperationDtos.PurchaseRequest;
import br.com.gestor360.dto.OperationDtos.PurchaseView;
import br.com.gestor360.service.OperationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/compras")
public class PurchaseController {
    private final OperationService service;
    public PurchaseController(OperationService service) { this.service = service; }
    @GetMapping public List<PurchaseView> list() { return service.purchases(); }
    @PostMapping public ResponseEntity<PurchaseView> create(@Valid @RequestBody PurchaseRequest request) {
        PurchaseView created = service.createPurchase(request);
        return ResponseEntity.created(URI.create("/api/compras/" + created.id())).body(created);
    }
    @PostMapping("/{id}/receber") public PurchaseView receive(@PathVariable long id) { return service.receivePurchase(id); }
    @PostMapping("/{id}/cancelar") public PurchaseView cancel(@PathVariable long id) { return service.cancelPurchase(id); }
}
