package br.com.gestor360.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity @Table(name = "vendas")
public class Sale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "cliente_id", foreignKey = @ForeignKey(name = "fk_venda_cliente")) private Customer customer;
    @Column(name = "total_centavos", nullable = false) private long totalCents;
    @Column(name = "forma_pagamento", nullable = false, length = 30) private String paymentMethod;
    @Column(name = "status", nullable = false, length = 20) private String status = "CONFIRMADA";
    @Column(name = "criada_em", nullable = false) private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SaleItem> items = new ArrayList<>();
    protected Sale() { }
    public Sale(Customer customer, long totalCents, String paymentMethod) { this.customer = customer; this.totalCents = totalCents; this.paymentMethod = paymentMethod; }
    public void addItem(SaleItem item) { items.add(item); item.setSale(this); }
    public void cancel() { status = "CANCELADA"; }
    public Long getId() { return id; }
    public Customer getCustomer() { return customer; }
    public long getTotalCents() { return totalCents; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<SaleItem> getItems() { return items; }
}
