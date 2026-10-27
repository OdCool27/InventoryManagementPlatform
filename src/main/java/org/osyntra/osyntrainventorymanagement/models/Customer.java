package org.osyntra.osyntrainventorymanagement.models;

public class Customer extends User {
    private String customerId;
    private String contactNumber;
    private Cart cart;

    //CONSTRUCTORS
    public Customer() {}

    public Customer(
            String firstName,
            String lastName,
            String email,
            String passwordHash,
            String customerId,
            String contactNumber,
            Cart cart
    ) {
        super(firstName, lastName, email, passwordHash);
        this.customerId = customerId;
        this.contactNumber = contactNumber;
        this.cart = cart;
    }

    public Customer(Customer customer) {
        super(customer);
        this.customerId = customer.customerId;
        this.contactNumber = customer.contactNumber;
        this.cart = customer.cart;
    }

    //ACCESSORS
    public String getCustomerId() {
        return customerId;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    //MUTATORS
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
}
