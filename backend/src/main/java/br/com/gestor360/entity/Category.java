package br.com.gestor360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "categorias", uniqueConstraints = @UniqueConstraint(name = "uk_categoria_nome", columnNames = "nome"))
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nome", nullable = false, length = 80)
    private String name;
    @Column(name = "descricao", length = 300)
    private String description;

    protected Category() { }
    public Category(String name, String description) { this.name = name; this.description = description; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public void update(String name, String description) { this.name = name; this.description = description; }
}
