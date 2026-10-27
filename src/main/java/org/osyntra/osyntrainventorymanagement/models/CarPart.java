package org.osyntra.osyntrainventorymanagement.models;

import java.math.BigDecimal;

public class CarPart extends Product{
    private String model;
    private String make;
    private String year;

    //CONSTRUCTORS
    CarPart(){
        super();
    }

    public CarPart(
            String sku,
            String barcode,
            String itemName,
            String itemDescription,
            BigDecimal unitPrice,
            String model,
            String make,
            String year
    ) {
        super(sku,barcode,itemName,itemDescription,unitPrice);
        this.model = model;
        this.make = make;
        this.year = year;
    }

    public CarPart(CarPart carPart) {
        super(carPart);
        this.model = carPart.model;
        this.make = carPart.make;
        this.year = carPart.year;
    }

    //ACCESSORS
    public String getModel() {
        return model;
    }

    public String getMake() {
        return make;
    }

    public String getYear() {
        return year;
    }

    //MUTATORS
    public void setModel(String model) {
        this.model = model;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
