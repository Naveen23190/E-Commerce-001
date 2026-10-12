package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_lines")
public class OrderLine {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "order_id")
    private CustomerOrder order;
    @ManyToOne(optional = false) @JoinColumn(name = "product_id")
    private Product product;
    @Column(nullable = false, length = 160)
    private String productName;
    @Column(nullable = false)
    private Integer quantity;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    protected OrderLine() {}
    public OrderLine(Product product, Integer quantity, BigDecimal unitPrice) {
        this.product=product; this.productName=product.getName(); this.quantity=quantity; this.unitPrice=unitPrice;
    }
    public void setOrder(CustomerOrder order){this.order=order;}
    public Long getId(){return id;} public Product getProduct(){return product;} public String getProductName(){return productName;}
    public Integer getQuantity(){return quantity;} public BigDecimal getUnitPrice(){return unitPrice;}
}
