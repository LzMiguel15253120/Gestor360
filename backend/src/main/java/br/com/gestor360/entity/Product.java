package br.com.gestor360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "produtos", uniqueConstraints = @UniqueConstraint(name = "uk_produto_codigo", columnNames = "codigo"))
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "codigo", nullable = false, length = 40)
    private String code;
    @Column(name = "nome", nullable = false, length = 120)
    private String name;
    @Column(name = "descricao", length = 500)
    private String description;
    @Column(name = "preco_centavos", nullable = false)
    private long priceCents;
    @Column(name = "estoque", nullable = false)
    private int stock;
    @Column(name = "estoque_minimo", nullable = false)
    private int minimumStock;
    @Column(name = "ativo", nullable = false)
    private boolean active = true;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", foreignKey = @ForeignKey(name = "fk_produto_categoria"))
    private Category category;

    protected Product() { }
    public Product(String code, String name, String description, long priceCents, int stock, int minimumStock, Category category) {
        this.code = code; this.name = name; this.description = description; this.priceCents = priceCents;
        this.stock = stock; this.minimumStock = minimumStock; this.category = category;
    }
    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public long getPriceCents() { return priceCents; }
    public int getStock() { return stock; }
    public int getMinimumStock() { return minimumStock; }
    public boolean isActive() { return active; }
    public Category getCategory() { return category; }
    public void update(String code, String name, String description, long priceCents, int minimumStock, Category category) {
        this.code = code; this.name = name; this.description = description; this.priceCents = priceCents;
        this.minimumStock = minimumStock; this.category = category;
    }
    public void deactivate() { active = false; }
    public void activate() { active = true; }
}
