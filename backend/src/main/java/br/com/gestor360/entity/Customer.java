package br.com.gestor360.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "clientes")
public class Customer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nome", nullable = false, length = 120)
    private String name;
    @Column(name = "email", length = 160)
    private String email;
    @Column(name = "telefone", length = 30)
    private String phone;
    protected Customer() { }
    public Customer(String name, String email, String phone) { this.name = name; this.email = email; this.phone = phone; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public void update(String name, String email, String phone) { this.name = name; this.email = email; this.phone = phone; }
}
