package com.example.ecommerce.controller;

import com.example.ecommerce.model.*;
import com.example.ecommerce.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartItemRepository cart; private final ProductRepository products; private final UserRepository users;
    public CartController(CartItemRepository cart,ProductRepository products,UserRepository users){this.cart=cart;this.products=products;this.users=users;}
    public record AddItem(Long productId,Integer quantity){}
    public record CartRow(Long productId,String name,BigDecimal price,Integer quantity,String imageUrl,BigDecimal subtotal){}
    public record CartResponse(List<CartRow> items,BigDecimal total){}
    @GetMapping public CartResponse get(@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.User principal){
        List<CartRow> rows=cart.findByUserUsername(principal.getUsername()).stream().map(i->new CartRow(i.getProduct().getId(),i.getProduct().getName(),i.getProduct().getPrice(),i.getQuantity(),i.getProduct().getImageData()==null?null:"/api/products/"+i.getProduct().getId()+"/image",i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))).toList();
        return new CartResponse(rows,rows.stream().map(CartRow::subtotal).reduce(BigDecimal.ZERO,BigDecimal::add));
    }
    @PostMapping("/items") @Transactional
    public CartResponse add(@RequestBody AddItem body,@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.User principal){
        if(body.productId()==null||body.quantity()==null||body.quantity()<1||body.quantity()>99)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Quantity must be 1-99");
        Product p=products.findById(body.productId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Product not found"));
        if(p.getStock()<body.quantity())throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient stock");
        AppUser u=users.findByUsername(principal.getUsername()).orElseThrow();
        CartItem item=cart.findByUserUsernameAndProductId(principal.getUsername(),p.getId()).orElse(null);
        if(item==null)cart.save(new CartItem(u,p,body.quantity())); else {int qty=item.getQuantity()+body.quantity();if(qty>p.getStock())throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient stock");item.setQuantity(qty);cart.save(item);}
        return get(principal);
    }
    @PutMapping("/items/{productId}") @Transactional
    public CartResponse set(@PathVariable Long productId,@RequestBody AddItem body,@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.User principal){
        if(body.quantity()==null||body.quantity()<1||body.quantity()>99)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Quantity must be 1-99");
        CartItem item=cart.findByUserUsernameAndProductId(principal.getUsername(),productId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
        if(body.quantity()>item.getProduct().getStock())throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient stock");
        item.setQuantity(body.quantity());cart.save(item);return get(principal);
    }
    @DeleteMapping("/items/{productId}") @Transactional
    public CartResponse remove(@PathVariable Long productId,@org.springframework.security.core.annotation.AuthenticationPrincipal org.springframework.security.core.userdetails.User principal){cart.deleteByUserUsernameAndProductId(principal.getUsername(),productId);return get(principal);}
}
