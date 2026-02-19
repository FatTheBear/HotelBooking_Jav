package com.hotel.hotelbooking.model;

public class Customer {
    private int customerId;
    private String fullName;
    private String phone;
    private String email;
    private String idCard;
    private String address;
    
    // Constructor with all fields
    public Customer(int customerId, String fullName, String phone, 
                    String email, String idCard, String address) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.idCard = idCard;
        this.address = address;
    }
    
    // Default constructor
    public Customer() {}
    
    // Getters
    public int getCustomerId() { return customerId; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getIdCard() { return idCard; }
    public String getAddress() { return address; }
    
    // Setters
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public void setAddress(String address) { this.address = address; }
    
    @Override
    public String toString() {
        return fullName + " (" + phone + ")";
    }
}