package com.example.batchdemo.entity;

/**
 * DTO for reading raw CSV data before transformation
 */
public class ProductInput {

    private String productId;
    private String name;
    private String description;
    private String price;
    private String quantity;

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return "ProductInput{productId='" + productId + "', name='" + name + "'}";
    }
}
