package br.com.gestor360.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import br.com.gestor360.dto.OperationDtos.DashboardView;
import br.com.gestor360.service.OperationService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final OperationService service;
    public DashboardController(OperationService service) { this.service = service; }
    @GetMapping public DashboardView get() { return service.dashboard(); }
}
