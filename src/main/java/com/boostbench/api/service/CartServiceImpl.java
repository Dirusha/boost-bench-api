package com.boostbench.api.service;

import com.boostbench.api.dto.CartDto;
import com.boostbench.api.dto.CartItemDto;
import com.boostbench.api.entity.Cart;
import com.boostbench.api.entity.CartItem;
import com.boostbench.api.entity.Product;
import com.boostbench.api.exception.ResourceNotFoundException;
import com.boostbench.api.repository.CartItemRepository;
import com.boostbench.api.repository.CartRepository;
import com.boostbench.api.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDto getCartByUserId(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));
        return convertToDto(cart);
    }

    @Override
    @Transactional
    public CartDto addItemToCart(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock for product: " + product.getName());
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        // Check if product is already in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update existing item quantity
            int newQuantity = existingItem.getQuantity() + quantity;
            if (product.getAvailableQuantity() < newQuantity) {
                throw new IllegalArgumentException("Total quantity exceeds available stock for product: " + product.getName());
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            // Add new cart item
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cart.getItems().add(cartItem);
            cartItemRepository.save(cartItem);
        }

        cartRepository.save(cart);
        return convertToDto(cart);
    }

    @Override
    @Transactional
    public CartDto updateCartItem(Long userId, Long cartItemId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        Product product = cartItem.getProduct();
        if (product.getAvailableQuantity() < quantity) {
            throw new IllegalArgumentException("Requested quantity exceeds available stock for product: " + product.getName());
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        return convertToDto(cart);
    }

    @Override
    @Transactional
    public CartDto removeItemFromCart(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with ID: " + cartItemId));

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        cartRepository.save(cart);
        return convertToDto(cart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user ID: " + userId));
        cart.getItems().clear();
        cartItemRepository.deleteAll(cart.getItems());
        cartRepository.save(cart);
    }

    private CartDto convertToDto(Cart cart) {
        CartDto cartDto = modelMapper.map(cart, CartDto.class);
        cartDto.setItems(cart.getItems().stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList()));
        return cartDto;
    }

    private CartItemDto convertToItemDto(CartItem cartItem) {
        CartItemDto itemDto = new CartItemDto();
        itemDto.setId(cartItem.getId());
        itemDto.setProductId(cartItem.getProduct().getId());
        itemDto.setProductName(cartItem.getProduct().getName());
        itemDto.setQuantity(cartItem.getQuantity());
        return itemDto;
    }
}