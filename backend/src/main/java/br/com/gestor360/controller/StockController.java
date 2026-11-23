package br.com.gestor360.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.gestor360.dto.OperationDtos.MovementView;
import br.com.gestor360.service.OperationService;

@RestController
@RequestMapping("/api/estoque")
public class StockController {
    private final OperationService service;
    public StockController(OperationService service) { this.service = service; }
    @GetMapping("/movimentacoes") public List<MovementView> movements() { return service.movements(); }
}
