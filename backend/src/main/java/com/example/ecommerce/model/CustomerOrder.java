package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) @JoinColumn(name = "user_id")
    private AppUser user;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;
    @Column(nullable = false, length = 30)
    private String status = "PLACED";
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderLine> items = new ArrayList<>();

    protected CustomerOrder() {}
    public CustomerOrder(AppUser user, BigDecimal total) { this.user=user; this.total=total; }
    public void addItem(OrderLine line) { items.add(line); line.setOrder(this); }
    public Long getId(){return id;} public AppUser getUser(){return user;} public BigDecimal getTotal(){return total;}
    public String getStatus(){return status;} public Instant getCreatedAt(){return createdAt;} public List<OrderLine> getItems(){return items;}
}
