package br.com.gestor360.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity @Table(name = "compras")
public class Purchase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "fornecedor_id", nullable = false, foreignKey = @ForeignKey(name = "fk_compra_fornecedor")) private Supplier supplier;
    @Column(name = "total_centavos", nullable = false) private long totalCents;
    @Column(name = "vencimento", nullable = false) private LocalDate dueDate;
    @Column(name = "status", nullable = false, length = 20) private String status = "PENDENTE";
    @Column(name = "criada_em", nullable = false) private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PurchaseItem> items = new ArrayList<>();
    protected Purchase() { }
    public Purchase(Supplier supplier, long totalCents, LocalDate dueDate) { this.supplier = supplier; this.totalCents = totalCents; this.dueDate = dueDate; }
    public void addItem(PurchaseItem item) { items.add(item); item.setPurchase(this); }
    public void receive() { status = "RECEBIDA"; }
    public void cancel() { status = "CANCELADA"; }
    public Long getId() { return id; }
    public Supplier getSupplier() { return supplier; }
    public long getTotalCents() { return totalCents; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<PurchaseItem> getItems() { return items; }
}
