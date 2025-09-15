package com.boostbench.api.service;

import com.boostbench.api.dto.CartDto;

public interface CartService {

    CartDto getCartByUserId(Long userId);

    CartDto addItemToCart(Long userId, Long productId, Integer quantity);

    CartDto updateCartItem(Long userId, Long cartItemId, Integer quantity);

    CartDto removeItemFromCart(Long userId, Long cartItemId);

    void clearCart(Long userId);
}