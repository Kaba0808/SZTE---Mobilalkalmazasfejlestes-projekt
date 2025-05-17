package com.example.museum;

public class MyTicketItem {

    private String type;
    private String description;
    private int price;
    private int quantity;

    public MyTicketItem(String type, String description, int price, int quantity) {
        this.type = type;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
