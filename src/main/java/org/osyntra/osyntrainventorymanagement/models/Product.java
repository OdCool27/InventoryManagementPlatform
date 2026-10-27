package org.osyntra.osyntrainventorymanagement.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public abstract class Product {
    private UUID itemId;
    private String sku;
    private String barcode;
    private String itemName;
    private String itemDescription;
    private BigDecimal unitPrice;
    private Instant addedAt;
    private Instant modifiedAt;

    //CONSTRUCTOR
    public Product() {}

    public Product(
            String sku,
            String barcode,
            String itemName,
            String itemDescription,
            BigDecimal unitPrice
    ){
        this.itemId = UUID.randomUUID();
        this.sku = sku;
        this.barcode = barcode;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.unitPrice = unitPrice;
        this.addedAt = Instant.now();
        this.modifiedAt = Instant.now();
    }

    public Product(Product product){
        this.itemId = product.itemId;
        this.sku = product.sku;
        this.barcode = product.barcode;
        this.itemName = product.itemName;
        this.itemDescription = product.itemDescription;
        this.unitPrice = product.unitPrice;
        this.addedAt = Instant.now();
        this.modifiedAt = Instant.now();
    }

    //ACCESSOR
    public UUID getItemId() {
        return itemId;
    }

    public String getSku() {
        return sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    //MUTATOR
    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
