package br.com.gestor360.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity @Table(name = "pagamentos")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "conta_id", nullable = false, foreignKey = @ForeignKey(name = "fk_pagamento_conta")) private Account account;
    @Column(name = "valor_centavos", nullable = false) private long amountCents;
    @Column(name = "forma_pagamento", nullable = false, length = 30) private String paymentMethod;
    @Column(name = "pago_em", nullable = false) private Instant paidAt = Instant.now();
    protected Payment() { }
    public Payment(long amountCents, String paymentMethod) { this.amountCents = amountCents; this.paymentMethod = paymentMethod; }
    public void setAccount(Account account) { this.account = account; }
    public Long getId() { return id; }
    public long getAmountCents() { return amountCents; }
    public String getPaymentMethod() { return paymentMethod; }
    public Instant getPaidAt() { return paidAt; }
    public Account getAccount() { return account; }
}
