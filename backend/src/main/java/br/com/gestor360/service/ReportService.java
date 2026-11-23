package br.com.gestor360.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.gestor360.dto.CatalogDtos.ProductView;
import br.com.gestor360.dto.OperationDtos.*;
import br.com.gestor360.entity.*;
import br.com.gestor360.repository.*;

@Service
public class ReportService {
    private final SaleRepository sales;
    private final PurchaseRepository purchases;
    private final ProductRepository products;
    private final PaymentRepository payments;

    public ReportService(SaleRepository sales, PurchaseRepository purchases, ProductRepository products, PaymentRepository payments) {
        this.sales = sales; this.purchases = purchases; this.products = products; this.payments = payments;
    }

    @Transactional(readOnly = true)
    public ReportView report(LocalDate startDate, LocalDate endDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant start = startDate.atStartOfDay(zone).toInstant();
        Instant end = endDate.plusDays(1).atStartOfDay(zone).toInstant();
        List<Sale> periodSales = sales.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);
        List<Purchase> periodPurchases = purchases.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end);
        List<Payment> periodPayments = payments.findByPaidAtGreaterThanEqualAndPaidAtLessThan(start, end);

        Map<String, long[]> top = new HashMap<>();
        for (Sale sale : periodSales) {
            if (!"CONFIRMADA".equals(sale.getStatus())) continue;
            for (SaleItem item : sale.getItems()) {
                long[] totals = top.computeIfAbsent(item.getProductName(), ignored -> new long[2]);
                totals[0] += item.getQuantity();
                totals[1] += Math.multiplyExact(item.getUnitPriceCents(), item.getQuantity());
            }
        }
        List<ReportProduct> bestSellers = top.entrySet().stream()
                .map(e -> new ReportProduct(e.getKey(), e.getValue()[0], e.getValue()[1]))
                .sorted(Comparator.comparingLong(ReportProduct::quantitySold).reversed()).limit(10).toList();
        long salesTotal = periodSales.stream().filter(s -> "CONFIRMADA".equals(s.getStatus())).mapToLong(Sale::getTotalCents).sum();
        long received = periodPayments.stream().filter(p -> "RECEBER".equals(p.getAccount().getType())).mapToLong(Payment::getAmountCents).sum();
        long paid = periodPayments.stream().filter(p -> "PAGAR".equals(p.getAccount().getType())).mapToLong(Payment::getAmountCents).sum();
        List<ProductView> stock = products.findAllByOrderByNameAsc().stream().map(this::view).toList();
        List<ReportSale> saleRows = periodSales.stream().sorted(Comparator.comparing(Sale::getCreatedAt).reversed())
                .map(s -> new ReportSale(s.getId(), s.getCustomer() == null ? "Consumidor final" : s.getCustomer().getName(), s.getTotalCents(), s.getStatus(), s.getCreatedAt())).toList();
        List<ReportPurchase> purchaseRows = periodPurchases.stream().sorted(Comparator.comparing(Purchase::getCreatedAt).reversed())
                .map(p -> new ReportPurchase(p.getId(), p.getSupplier().getName(), p.getTotalCents(), p.getStatus(), p.getCreatedAt())).toList();
        return new ReportView(startDate, endDate, periodSales.stream().filter(s -> "CONFIRMADA".equals(s.getStatus())).count(),
                salesTotal, received, paid, bestSellers, stock, saleRows, purchaseRows);
    }

    private ProductView view(Product p) {
        return new ProductView(p.getId(), p.getCode(), p.getName(), p.getDescription(), p.getPriceCents(), p.getStock(), p.getMinimumStock(), p.isActive(), p.getCategory() == null ? null : p.getCategory().getId(), p.getCategory() == null ? null : p.getCategory().getName());
    }
}
