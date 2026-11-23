package br.com.gestor360.entity;

import jakarta.persistence.*;

@Entity @Table(name = "itens_venda")
public class SaleItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "produto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_venda_produto")) private Product product;
    @Column(name = "nome_produto", nullable = false, length = 120) private String productName;
    @Column(name = "quantidade", nullable = false) private int quantity;
    @Column(name = "preco_unitario_centavos", nullable = false) private long unitPriceCents;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "venda_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_venda_venda")) private Sale sale;
    protected SaleItem() { }
    public SaleItem(Product product, int quantity, long unitPriceCents) { this.product = product; this.productName = product.getName(); this.quantity = quantity; this.unitPriceCents = unitPriceCents; }
    public void setSale(Sale sale) { this.sale = sale; }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public long getUnitPriceCents() { return unitPriceCents; }
}
