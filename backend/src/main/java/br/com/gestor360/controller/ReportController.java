package br.com.gestor360.controller;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.springframework.web.bind.annotation.*;
import br.com.gestor360.dto.OperationDtos.ReportView;
import br.com.gestor360.exception.BusinessRuleException;
import br.com.gestor360.service.ReportService;

@RestController
@RequestMapping("/api/relatorios")
public class ReportController {
    private final ReportService service;
    public ReportController(ReportService service) { this.service = service; }
    @GetMapping public ReportView get(@RequestParam(required = false) LocalDate inicio, @RequestParam(required = false) LocalDate fim) {
        LocalDate today = LocalDate.now();
        LocalDate start = inicio == null ? today.withDayOfMonth(1) : inicio;
        LocalDate end = fim == null ? today : fim;
        if (start.isAfter(end)) throw new BusinessRuleException("A data inicial deve ser anterior ou igual à data final.");
        if (ChronoUnit.DAYS.between(start, end) > 366) throw new BusinessRuleException("O período máximo do relatório é de 367 dias.");
        return service.report(start, end);
    }
}
