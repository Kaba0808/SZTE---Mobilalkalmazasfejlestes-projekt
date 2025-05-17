package com.example.museum;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static final List<CartItem> cartItems = new ArrayList<>();

    public static void addToCart(CartItem newItem) {
        // Ellenőrizzük, hogy van-e már ilyen típusú jegy a kosárban
        for (CartItem item : cartItems) {
            if (item.getTicketType().equals(newItem.getTicketType())) {
                // Ha van, növeljük a mennyiséget
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                return;
            }
        }
        // Ha nincs, hozzáadjuk az új jegyet
        cartItems.add(newItem);
    }

    public static List<CartItem> getCartItems() {
        return cartItems;
    }

    public static void removeFromCart(CartItem item) {
        cartItems.remove(item);
    }

    public static void clearCart() {
        cartItems.clear();
    }
}

