package br.com.gestor360.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import br.com.gestor360.dto.CatalogDtos.ProductView;

public final class OperationDtos {
    private OperationDtos() { }
    public record SaleItemRequest(@NotNull(message = "Selecione um produto.") Long productId,
                                  @Min(value = 1, message = "A quantidade deve ser maior que zero.") int quantity) { }
    public record SaleRequest(Long customerId,
                              @NotEmpty(message = "Adicione pelo menos um produto.") List<@Valid SaleItemRequest> items,
                              @NotBlank(message = "Selecione a forma de pagamento.") String paymentMethod, LocalDate dueDate) { }
    public record SaleItemView(Long productId, String productName, int quantity, long unitPriceCents, long subtotalCents) { }
    public record SaleView(Long id, Long customerId, String customerName, long totalCents, String paymentMethod,
                           String status, boolean cancellable, Instant createdAt, List<SaleItemView> items) { }
    public record PurchaseItemRequest(@NotNull(message = "Selecione um produto.") Long productId,
                                      @Min(value = 1, message = "A quantidade deve ser maior que zero.") int quantity,
                                      @Min(value = 0, message = "O custo não pode ser negativo.") long unitPriceCents) { }
    public record PurchaseRequest(@NotNull(message = "Selecione um fornecedor.") Long supplierId,
                                  @NotEmpty(message = "Adicione pelo menos um produto.") List<@Valid PurchaseItemRequest> items,
                                  @NotNull(message = "Informe o vencimento.") LocalDate dueDate) { }
    public record PurchaseItemView(Long productId, String productName, int quantity, long unitCostCents, long subtotalCents) { }
    public record PurchaseView(Long id, Long supplierId, String supplierName, long totalCents, LocalDate dueDate,
                               String status, Instant createdAt, List<PurchaseItemView> items) { }
    public record PaymentRequest(@Min(value = 1, message = "O pagamento deve ser maior que zero.") long amountCents,
                                 @NotBlank(message = "Selecione a forma de pagamento.") String paymentMethod) { }
    public record AccountView(Long id, String type, String description, long amountCents, long paidCents,
                              long balanceCents, LocalDate dueDate, String status, boolean overdue) { }
    public record PaymentView(Long id, long amountCents, String paymentMethod, Instant paidAt) { }
    public record MovementView(Long id, Long productId, String productName, int quantityDelta,
                               String referenceType, Long referenceId, String type, Instant createdAt) { }
    public record DashboardView(long salesTodayCents, long receivedTodayCents, long paidTodayCents,
                                long pendingReceivablesCents, long pendingPayablesCents, long lowStockCount,
                                List<SaleView> recentSales) { }
    public record ReportProduct(String name, long quantitySold, long totalCents) { }
    public record ReportSale(Long id, String customerName, long totalCents, String status, Instant createdAt) { }
    public record ReportPurchase(Long id, String supplierName, long totalCents, String status, Instant createdAt) { }
    public record ReportView(LocalDate startDate, LocalDate endDate, long salesCount, long salesTotalCents,
                             long receivablesPaidCents, long payablesPaidCents, List<ReportProduct> bestSellers,
                             List<ProductView> stock, List<ReportSale> sales, List<ReportPurchase> purchases) { }
}
