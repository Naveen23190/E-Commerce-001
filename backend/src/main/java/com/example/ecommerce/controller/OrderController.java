package com.example.ecommerce.controller;

import com.example.ecommerce.model.*;
import com.example.ecommerce.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final CartItemRepository cart; private final OrderRepository orders; private final UserRepository users; private final ProductRepository products;
    public OrderController(CartItemRepository cart,OrderRepository orders,UserRepository users,ProductRepository products){this.cart=cart;this.orders=orders;this.users=users;this.products=products;}
    public record OrderLineResponse(String productName,Integer quantity,BigDecimal unitPrice){}
    public record OrderResponse(Long id,BigDecimal total,String status,Instant createdAt,List<OrderLineResponse> items){}
    private OrderResponse dto(CustomerOrder o){return new OrderResponse(o.getId(),o.getTotal(),o.getStatus(),o.getCreatedAt(),o.getItems().stream().map(i->new OrderLineResponse(i.getProductName(),i.getQuantity(),i.getUnitPrice())).toList());}
    @PostMapping @Transactional
    public OrderResponse place(@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.User principal){
        List<CartItem> items=cart.findByUserUsername(principal.getUsername());
        if(items.isEmpty())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Cart is empty");
        BigDecimal total=BigDecimal.ZERO;
        for(CartItem i:items){Product p=i.getProduct();if(p.getStock()<i.getQuantity())throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient stock for "+p.getName());total=total.add(p.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())));}
        AppUser user=users.findByUsername(principal.getUsername()).orElseThrow();
        CustomerOrder order=new CustomerOrder(user,total);
        for(CartItem i:items){Product p=i.getProduct();order.addItem(new OrderLine(p,i.getQuantity(),p.getPrice()));p.setStock(p.getStock()-i.getQuantity());products.save(p);}
        CustomerOrder saved=orders.save(order);cart.deleteByUserUsername(principal.getUsername());return dto(saved);
    }
    @GetMapping @Transactional(readOnly = true) public List<OrderResponse> mine(@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.User principal){return orders.findByUserUsernameOrderByCreatedAtDesc(principal.getUsername()).stream().map(this::dto).toList();}
}
