package com.example.ecommerce.repo;
import com.example.ecommerce.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserUsername(String username);
    Optional<CartItem> findByUserUsernameAndProductId(String username, Long productId);
    void deleteByUserUsernameAndProductId(String username, Long productId);
    void deleteByUserUsername(String username);
}
