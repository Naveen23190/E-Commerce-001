package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(length = 3000)
    private String description;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    @Column(nullable = false)
    private Integer stock = 0;

    // PostgreSQL BYTEA: image bytes are stored in the database itself.
    @Column(name = "image_data", columnDefinition = "bytea")
    private byte[] imageData;
    @Column(name = "image_content_type", length = 100)
    private String imageContentType;

    protected Product() {}
    public Product(String name, String description, BigDecimal price, Integer stock, byte[] imageData, String imageContentType) {
        this.name = name; this.description = description; this.price = price; this.stock = stock;
        this.imageData = imageData; this.imageContentType = imageContentType;
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Integer getStock() { return stock; }
    public byte[] getImageData() { return imageData; }
    public String getImageContentType() { return imageContentType; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(Integer stock) { this.stock = stock; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public void setImageContentType(String imageContentType) { this.imageContentType = imageContentType; }
}
