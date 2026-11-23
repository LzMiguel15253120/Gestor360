package br.com.gestor360.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

@Entity @Table(name = "contas")
public class Account {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Version private long version;
    @Column(name = "tipo", nullable = false, length = 20) private String type;
    @Column(name = "descricao", nullable = false, length = 180) private String description;
    @Column(name = "valor_centavos", nullable = false) private long amountCents;
    @Column(name = "pago_centavos", nullable = false) private long paidCents;
    @Column(name = "vencimento", nullable = false) private LocalDate dueDate;
    @Column(name = "status", nullable = false, length = 20) private String status = "PENDENTE";
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "venda_id", unique = true, foreignKey = @ForeignKey(name = "fk_conta_venda")) private Sale sale;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "compra_id", unique = true, foreignKey = @ForeignKey(name = "fk_conta_compra")) private Purchase purchase;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments = new ArrayList<>();
    protected Account() { }
    public Account(String type, String description, long amountCents, LocalDate dueDate, Sale sale, Purchase purchase) {
        this.type = type; this.description = description; this.amountCents = amountCents; this.dueDate = dueDate; this.sale = sale; this.purchase = purchase;
    }
    public void addPayment(Payment payment) {
        paidCents = Math.addExact(paidCents, payment.getAmountCents());
        status = paidCents == amountCents ? "PAGA" : "PARCIAL";
        payments.add(payment); payment.setAccount(this);
    }
    public void cancel() { status = "CANCELADA"; }
    public Long getId() { return id; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public long getAmountCents() { return amountCents; }
    public long getPaidCents() { return paidCents; }
    public long getBalanceCents() { return amountCents - paidCents; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }
}
