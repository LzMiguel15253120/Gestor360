package br.com.gestor360.entity;

import java.time.Instant;
import jakarta.persistence.*;

@Entity
@Table(name = "movimentacoes_estoque", uniqueConstraints = @UniqueConstraint(name = "uk_movimento_referencia", columnNames = {"referencia_tipo", "referencia_id", "produto_id", "tipo"}))
public class StockMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "produto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_movimento_produto")) private Product product;
    @Column(name = "quantidade_delta", nullable = false) private int quantityDelta;
    @Column(name = "referencia_tipo", nullable = false, length = 30) private String referenceType;
    @Column(name = "referencia_id", nullable = false) private long referenceId;
    @Column(name = "tipo", nullable = false, length = 30) private String type;
    @Column(name = "criada_em", nullable = false) private Instant createdAt = Instant.now();
    protected StockMovement() { }
    public StockMovement(Product product, int quantityDelta, String referenceType, long referenceId, String type) {
        this.product = product; this.quantityDelta = quantityDelta; this.referenceType = referenceType; this.referenceId = referenceId; this.type = type;
    }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public int getQuantityDelta() { return quantityDelta; }
    public String getReferenceType() { return referenceType; }
    public long getReferenceId() { return referenceId; }
    public String getType() { return type; }
    public Instant getCreatedAt() { return createdAt; }
}
