package com.hotel.hotelbooking.model;

import java.time.LocalDate;

public class Booking {
    private int bookingId;
    private int customerId;
    private int roomId;
    private LocalDate checkinDate;
    private LocalDate checkoutDate;
    private double totalPrice;
    private String status;
    
    // For display purposes (optional - to show customer name and room number)
    private String customerName;
    private String roomNumber;
    
    // Constructor with all fields
    public Booking(int bookingId, int customerId, int roomId,
                   LocalDate checkinDate, LocalDate checkoutDate,
                   double totalPrice, String status) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.roomId = roomId;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
        this.totalPrice = totalPrice;
        this.status = status;
    }
    
    // Default constructor
    public Booking() {}
    
    // Getters
    public int getBookingId() { return bookingId; }
    public int getCustomerId() { return customerId; }
    public int getRoomId() { return roomId; }
    public LocalDate getCheckinDate() { return checkinDate; }
    public LocalDate getCheckoutDate() { return checkoutDate; }
    public double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getCustomerName() { return customerName; }
    public String getRoomNumber() { return roomNumber; }
    
    // Setters
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public void setCheckinDate(LocalDate checkinDate) { this.checkinDate = checkinDate; }
    public void setCheckoutDate(LocalDate checkoutDate) { this.checkoutDate = checkoutDate; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setStatus(String status) { this.status = status; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    
    @Override
    public String toString() {
        return "Booking #" + bookingId + " - " + status;
    }
}