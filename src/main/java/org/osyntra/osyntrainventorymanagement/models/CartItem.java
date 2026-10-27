package org.osyntra.osyntrainventorymanagement.models;

import java.math.BigDecimal;

public class CartItem {
    String sku;
    String productName;
    String productDescription;
    Integer quantity;
    BigDecimal unitPrice;

    //CONSTRUCTORS
    public CartItem(){}

    public CartItem(Product p, Integer quantity) {
        this.sku = p.getSku();
        this.productName = p.getItemName();
        this.productDescription = p.getItemDescription();
        this.quantity = quantity;
        this.unitPrice = p.getUnitPrice();
    }


    //ACCESSORS
    public String getSku() {
        return sku;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public Integer getQuantity() {
        return quantity;
    }

    //MUTATORS
    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }


    //Calculate Item Total
    public BigDecimal calculateItemTotal(){
        BigDecimal itemTotal = new BigDecimal(0);

        itemTotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity));
        return itemTotal;
    }
}
