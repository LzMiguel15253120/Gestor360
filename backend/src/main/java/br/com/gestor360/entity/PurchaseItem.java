package br.com.gestor360.entity;

import jakarta.persistence.*;

@Entity @Table(name = "itens_compra")
public class PurchaseItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.EAGER, optional = false) @JoinColumn(name = "produto_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_compra_produto")) private Product product;
    @Column(name = "nome_produto", nullable = false, length = 120) private String productName;
    @Column(name = "quantidade", nullable = false) private int quantity;
    @Column(name = "custo_unitario_centavos", nullable = false) private long unitCostCents;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "compra_id", nullable = false, foreignKey = @ForeignKey(name = "fk_item_compra_compra")) private Purchase purchase;
    protected PurchaseItem() { }
    public PurchaseItem(Product product, int quantity, long unitCostCents) { this.product = product; this.productName = product.getName(); this.quantity = quantity; this.unitCostCents = unitCostCents; }
    public void setPurchase(Purchase purchase) { this.purchase = purchase; }
    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public long getUnitCostCents() { return unitCostCents; }
}
