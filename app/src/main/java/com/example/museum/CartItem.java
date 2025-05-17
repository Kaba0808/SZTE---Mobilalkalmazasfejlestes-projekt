package com.example.museum;

public class CartItem {
    private String ticketType;
    private String description;
    private int unitPrice;
    private int quantity;


    public CartItem(String ticketType, String description,  int unitPrice, int quantity) {
        this.ticketType = ticketType;
        this.description = description;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public CartItem(String type, int price, int quantity) {
    }

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
