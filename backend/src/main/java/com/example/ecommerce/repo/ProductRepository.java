package com.example.ecommerce.repo;
import com.example.ecommerce.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProductRepository extends JpaRepository<Product, Long> {}
