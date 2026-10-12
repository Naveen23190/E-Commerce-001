package com.example.ecommerce.model;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private AppUser user;
    @ManyToOne(optional = false, fetch = FetchType.EAGER) @JoinColumn(name = "product_id")
    private Product product;
    @Column(nullable = false)
    private Integer quantity;

    protected CartItem() {}
    public CartItem(AppUser user, Product product, Integer quantity) { this.user=user; this.product=product; this.quantity=quantity; }
    public Long getId(){return id;} public AppUser getUser(){return user;} public Product getProduct(){return product;} public Integer getQuantity(){return quantity;}
    public void setQuantity(Integer quantity){this.quantity=quantity;}
}
