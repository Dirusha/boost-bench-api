package com.boostbench.api.controller;

import com.boostbench.api.dto.CartDto;
import com.boostbench.api.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('CART_READ')")
    public ResponseEntity<CartDto> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/{userId}/items")
    @PreAuthorize("hasAuthority('CART_MODIFY')")
    public ResponseEntity<CartDto> addItemToCart(
            @PathVariable Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return new ResponseEntity<>(cartService.addItemToCart(userId, productId, quantity), HttpStatus.CREATED);
    }

    @PutMapping("/{userId}/items/{cartItemId}")
    @PreAuthorize("hasAuthority('CART_MODIFY')")
    public ResponseEntity<CartDto> updateCartItem(
            @PathVariable Long userId,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateCartItem(userId, cartItemId, quantity));
    }

    @DeleteMapping("/{userId}/items/{cartItemId}")
    @PreAuthorize("hasAuthority('CART_MODIFY')")
    public ResponseEntity<CartDto> removeItemFromCart(
            @PathVariable Long userId,
            @PathVariable Long cartItemId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(userId, cartItemId));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('CART_MODIFY')")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}