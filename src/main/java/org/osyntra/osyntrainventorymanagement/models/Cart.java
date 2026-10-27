package org.osyntra.osyntrainventorymanagement.models;

import java.math.BigDecimal;
import java.util.List;

public class Cart {
    private List<CartItem> cartItems;
    private BigDecimal subTotal;

    //CONSTRUCTORS
    public Cart(){}

    public Cart(List<CartItem> cartItems) {
        this.cartItems = cartItems;
        calculateSubTotal();
    }

    //ACCESSOR
    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    //MUTATOR
    public void setProducts(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }


    //Calculate Subtotal based on items in cart
    public void calculateSubTotal() {

        this.subTotal = BigDecimal.ZERO;

        for(CartItem c: this.cartItems){
            subTotal = subTotal.add(c.calculateItemTotal());
        }

    }
}
