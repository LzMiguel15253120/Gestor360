package br.com.gestor360.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import br.com.gestor360.dto.OperationDtos.AccountView;
import br.com.gestor360.dto.OperationDtos.PaymentRequest;
import br.com.gestor360.dto.OperationDtos.PaymentView;
import br.com.gestor360.service.OperationService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/financeiro")
public class FinanceController {
    private final OperationService service;
    public FinanceController(OperationService service) { this.service = service; }
    @GetMapping("/contas") public List<AccountView> accounts(@RequestParam(required = false) String tipo, @RequestParam(required = false) String status) {
        return service.accounts(tipo, status);
    }
    @GetMapping("/contas/{id}/pagamentos") public List<PaymentView> payments(@PathVariable long id) { return service.payments(id); }
    @PostMapping("/contas/{id}/pagamentos") public List<PaymentView> pay(@PathVariable long id, @Valid @RequestBody PaymentRequest request) {
        return service.registerPayment(id, request);
    }
}
