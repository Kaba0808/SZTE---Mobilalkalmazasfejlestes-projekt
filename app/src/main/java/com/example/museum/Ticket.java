package com.example.museum;

public class Ticket {
    private String type;
    private double price;
    private String description;
    private int quantity;

    public Ticket(String type, int price, String description) {
        this.type = type;
        this.price = price;
        this.description = description;
        this.quantity = 0;
    }

    public String getType() {
        return type;
    }

    public int getPrice() {
        return (int) price;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void increaseQuantity() {
        this.quantity++;
    }

    public void decreaseQuantity() {
        this.quantity--;
    }
}
