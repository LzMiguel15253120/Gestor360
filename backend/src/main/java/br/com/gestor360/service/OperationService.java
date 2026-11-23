package br.com.gestor360.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.gestor360.dto.OperationDtos.*;
import br.com.gestor360.entity.*;
import br.com.gestor360.exception.BusinessRuleException;
import br.com.gestor360.exception.ResourceNotFoundException;
import br.com.gestor360.repository.*;

@Service
@Transactional
public class OperationService {
    private static final Set<String> PAYMENT_METHODS = Set.of("PIX", "DINHEIRO", "CARTAO_CREDITO", "CARTAO_DEBITO", "TRANSFERENCIA", "A_PRAZO");
    private final ProductRepository products;
    private final CustomerRepository customers;
    private final SupplierRepository suppliers;
    private final SaleRepository sales;
    private final PurchaseRepository purchases;
    private final StockMovementRepository movements;
    private final AccountRepository accounts;
    private final PaymentRepository payments;

    public OperationService(ProductRepository products, CustomerRepository customers, SupplierRepository suppliers,
                            SaleRepository sales, PurchaseRepository purchases, StockMovementRepository movements,
                            AccountRepository accounts, PaymentRepository payments) {
        this.products = products; this.customers = customers; this.suppliers = suppliers; this.sales = sales;
        this.purchases = purchases; this.movements = movements; this.accounts = accounts; this.payments = payments;
    }

    @Transactional(readOnly = true)
    public List<SaleView> sales() { return sales.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList(); }
    @Transactional(readOnly = true)
    public SaleView sale(long id) { return view(saleEntity(id)); }

    public SaleView createSale(SaleRequest request) {
        String method = normalizeMethod(request.paymentMethod());
        Customer customer = request.customerId() == null ? null : customers.findById(request.customerId()).orElseThrow(() -> notFound("Cliente", request.customerId()));
        if ("A_PRAZO".equals(method) && (customer == null || request.dueDate() == null))
            throw new BusinessRuleException("Venda a prazo exige cliente e data de vencimento.");
        Set<Long> unique = new HashSet<>();
        List<Product> selected = new ArrayList<>();
        long total = 0;
        for (SaleItemRequest line : request.items()) {
            if (!unique.add(line.productId())) throw new BusinessRuleException("Cada produto deve aparecer uma única vez na venda.");
            Product product = products.findById(line.productId()).orElseThrow(() -> notFound("Produto", line.productId()));
            if (!product.isActive()) throw new BusinessRuleException("O produto " + product.getName() + " está inativo.");
            if (products.reduceStock(product.getId(), line.quantity()) != 1)
                throw new BusinessRuleException("Estoque insuficiente para " + product.getName() + ".");
            total = Math.addExact(total, Math.multiplyExact(product.getPriceCents(), line.quantity()));
            selected.add(product);
        }
        Sale sale = new Sale(customer, total, method);
        for (int i = 0; i < selected.size(); i++) {
            Product product = selected.get(i);
            int quantity = request.items().get(i).quantity();
            sale.addItem(new SaleItem(product, quantity, product.getPriceCents()));
        }
        sale = sales.saveAndFlush(sale);
        for (int i = 0; i < selected.size(); i++) {
            Product product = products.findById(selected.get(i).getId()).orElseThrow();
            movements.save(new StockMovement(product, -request.items().get(i).quantity(), "VENDA", sale.getId(), "SAIDA_VENDA"));
        }
        LocalDate dueDate = request.dueDate() == null ? LocalDate.now() : request.dueDate();
        Account account = new Account("RECEBER", "Venda #" + sale.getId(), total, dueDate, sale, null);
        if (!"A_PRAZO".equals(method)) account.addPayment(new Payment(total, method));
        accounts.save(account);
        return view(sales.findById(sale.getId()).orElseThrow());
    }

    public SaleView cancelSale(long id) {
        Sale sale = saleEntity(id);
        if (!"CONFIRMADA".equals(sale.getStatus())) throw new BusinessRuleException("A venda já foi cancelada.");
        Account account = accounts.findBySaleId(id).orElseThrow();
        if (account.getPaidCents() > 0) throw new BusinessRuleException("Não é possível cancelar uma venda com pagamentos registrados. Registre o estorno financeiro antes.");
        for (SaleItem item : sale.getItems()) {
            if (movements.existsByProductIdAndReferenceTypeAndReferenceIdAndType(item.getProduct().getId(), "VENDA", id, "ESTORNO_VENDA"))
                throw new BusinessRuleException("O estoque desta venda já foi devolvido.");
            if (products.increaseStock(item.getProduct().getId(), item.getQuantity()) != 1)
                throw new BusinessRuleException("Não foi possível devolver o estoque do produto " + item.getProductName() + ".");
            Product product = products.findById(item.getProduct().getId()).orElseThrow();
            movements.save(new StockMovement(product, item.getQuantity(), "VENDA", id, "ESTORNO_VENDA"));
        }
        sale.cancel(); account.cancel();
        sales.save(sale); accounts.save(account);
        return view(sale);
    }

    @Transactional(readOnly = true)
    public List<PurchaseView> purchases() { return purchases.findAllByOrderByCreatedAtDesc().stream().map(this::view).toList(); }
    public PurchaseView createPurchase(PurchaseRequest request) {
        Supplier supplier = suppliers.findById(request.supplierId()).orElseThrow(() -> notFound("Fornecedor", request.supplierId()));
        Set<Long> unique = new HashSet<>();
        List<Product> selected = new ArrayList<>();
        long total = 0;
        for (PurchaseItemRequest line : request.items()) {
            if (!unique.add(line.productId())) throw new BusinessRuleException("Cada produto deve aparecer uma única vez na compra.");
            Product product = products.findById(line.productId()).orElseThrow(() -> notFound("Produto", line.productId()));
            if (!product.isActive()) throw new BusinessRuleException("O produto " + product.getName() + " está inativo e não pode ser comprado.");
            total = Math.addExact(total, Math.multiplyExact(line.unitPriceCents(), line.quantity()));
            selected.add(product);
        }
        Purchase purchase = new Purchase(supplier, total, request.dueDate());
        for (int i = 0; i < selected.size(); i++) {
            PurchaseItemRequest line = request.items().get(i);
            purchase.addItem(new PurchaseItem(selected.get(i), line.quantity(), line.unitPriceCents()));
        }
        return view(purchases.save(purchase));
    }
    public PurchaseView receivePurchase(long id) {
        Purchase purchase = purchases.findById(id).orElseThrow(() -> notFound("Compra", id));
        if (!"PENDENTE".equals(purchase.getStatus())) throw new BusinessRuleException("A compra já foi recebida.");
        purchase.receive();
        for (PurchaseItem item : purchase.getItems()) {
            if (products.increaseStock(item.getProduct().getId(), item.getQuantity()) != 1)
                throw new BusinessRuleException("Não foi possível atualizar o estoque do produto " + item.getProductName() + ".");
            Product product = products.findById(item.getProduct().getId()).orElseThrow();
            movements.save(new StockMovement(product, item.getQuantity(), "COMPRA", id, "ENTRADA_COMPRA"));
        }
        accounts.save(new Account("PAGAR", "Compra #" + id, purchase.getTotalCents(), purchase.getDueDate(), null, purchase));
        return view(purchase);
    }
    public PurchaseView cancelPurchase(long id) {
        Purchase purchase = purchases.findById(id).orElseThrow(() -> notFound("Compra", id));
        if (!"PENDENTE".equals(purchase.getStatus())) throw new BusinessRuleException("Somente compras pendentes podem ser canceladas.");
        purchase.cancel();
        return view(purchase);
    }

    @Transactional(readOnly = true)
    public List<AccountView> accounts(String type, String status) {
        List<Account> result = accounts.findAllByOrderByDueDateAsc();
        if (type != null && !type.isBlank()) result = result.stream().filter(a -> a.getType().equals(type.toUpperCase(Locale.ROOT))).toList();
        if (status != null && !status.isBlank()) result = result.stream().filter(a -> a.getStatus().equals(status.toUpperCase(Locale.ROOT))).toList();
        return result.stream().map(this::view).toList();
    }
    @Transactional(readOnly = true)
    public List<PaymentView> payments(long accountId) {
        if (!accounts.existsById(accountId)) throw notFound("Conta", accountId);
        return payments.findByAccountIdOrderByPaidAtDesc(accountId).stream().map(p -> new PaymentView(p.getId(), p.getAmountCents(), p.getPaymentMethod(), p.getPaidAt())).toList();
    }
    public List<PaymentView> registerPayment(long accountId, PaymentRequest request) {
        Account account = accounts.findById(accountId).orElseThrow(() -> notFound("Conta", accountId));
        if ("CANCELADA".equals(account.getStatus())) throw new BusinessRuleException("Não é possível pagar uma conta cancelada.");
        if (request.amountCents() > account.getBalanceCents()) throw new BusinessRuleException("O pagamento excede o saldo pendente.");
        String method = normalizeMethod(request.paymentMethod());
        if ("A_PRAZO".equals(method)) throw new BusinessRuleException("Selecione a forma usada para este pagamento.");
        account.addPayment(new Payment(request.amountCents(), method));
        accounts.save(account);
        return payments(accountId);
    }

    @Transactional(readOnly = true)
    public List<MovementView> movements() { return movements.findTop100ByOrderByCreatedAtDesc().stream().map(m -> new MovementView(m.getId(), m.getProduct().getId(), m.getProduct().getName(), m.getQuantityDelta(), m.getReferenceType(), m.getReferenceId(), m.getType(), m.getCreatedAt())).toList(); }

    @Transactional(readOnly = true)
    public DashboardView dashboard() {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        Instant start = today.atStartOfDay(zone).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(zone).toInstant();
        long salesToday = sales.findByCreatedAtGreaterThanEqualAndCreatedAtLessThan(start, end).stream()
                .filter(s -> "CONFIRMADA".equals(s.getStatus())).mapToLong(Sale::getTotalCents).sum();
        List<Payment> todayPayments = payments.findByPaidAtGreaterThanEqualAndPaidAtLessThan(start, end);
        long received = todayPayments.stream().filter(p -> "RECEBER".equals(accountFor(p).getType())).mapToLong(Payment::getAmountCents).sum();
        long paid = todayPayments.stream().filter(p -> "PAGAR".equals(accountFor(p).getType())).mapToLong(Payment::getAmountCents).sum();
        long pendingReceivables = accounts.findByTypeAndStatusNot("RECEBER", "CANCELADA").stream().mapToLong(Account::getBalanceCents).sum();
        long pendingPayables = accounts.findByTypeAndStatusNot("PAGAR", "CANCELADA").stream().mapToLong(Account::getBalanceCents).sum();
        long lowStock = products.countActiveLowStock();
        return new DashboardView(salesToday, received, paid, pendingReceivables, pendingPayables, lowStock,
                sales.findTop10ByOrderByCreatedAtDesc().stream().map(this::view).toList());
    }

    private Account accountFor(Payment payment) {
        return payment.getAccount();
    }
    private Sale saleEntity(long id) { return sales.findById(id).orElseThrow(() -> notFound("Venda", id)); }
    private String normalizeMethod(String method) {
        String normalized = method.trim().toUpperCase(Locale.ROOT);
        if (!PAYMENT_METHODS.contains(normalized)) throw new BusinessRuleException("Forma de pagamento inválida.");
        return normalized;
    }
    private SaleView view(Sale s) {
        List<SaleItemView> lines = s.getItems().stream().map(i -> new SaleItemView(i.getProduct().getId(), i.getProductName(), i.getQuantity(), i.getUnitPriceCents(), Math.multiplyExact(i.getUnitPriceCents(), i.getQuantity()))).toList();
        boolean cancellable = "CONFIRMADA".equals(s.getStatus()) && accounts.findBySaleId(s.getId()).map(a -> a.getPaidCents() == 0).orElse(false);
        return new SaleView(s.getId(), s.getCustomer() == null ? null : s.getCustomer().getId(), s.getCustomer() == null ? "Consumidor final" : s.getCustomer().getName(), s.getTotalCents(), s.getPaymentMethod(), s.getStatus(), cancellable, s.getCreatedAt(), lines);
    }
    private PurchaseView view(Purchase p) {
        List<PurchaseItemView> lines = p.getItems().stream().map(i -> new PurchaseItemView(i.getProduct().getId(), i.getProductName(), i.getQuantity(), i.getUnitCostCents(), Math.multiplyExact(i.getUnitCostCents(), i.getQuantity()))).toList();
        return new PurchaseView(p.getId(), p.getSupplier().getId(), p.getSupplier().getName(), p.getTotalCents(), p.getDueDate(), p.getStatus(), p.getCreatedAt(), lines);
    }
    private AccountView view(Account a) {
        boolean overdue = ("PENDENTE".equals(a.getStatus()) || "PARCIAL".equals(a.getStatus())) && a.getDueDate().isBefore(LocalDate.now());
        return new AccountView(a.getId(), a.getType(), a.getDescription(), a.getAmountCents(), a.getPaidCents(), a.getBalanceCents(), a.getDueDate(), overdue ? "VENCIDA" : a.getStatus(), overdue);
    }
    private ResourceNotFoundException notFound(String type, long id) { return new ResourceNotFoundException(type + " " + id + " não encontrado."); }
}
