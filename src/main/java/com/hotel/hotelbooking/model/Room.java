package com.hotel.hotelbooking.model;

public class Room {
    private int roomId;
    private String roomNumber;
    private String roomType;
    private double price;
    private String status;
    private String description;
    
    // Constructor with all fields
    public Room(int roomId, String roomNumber, String roomType, 
                double price, String status, String description) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.price = price;
        this.status = status;
        this.description = description;
    }
    
    // Default constructor
    public Room() {}
    
    // Getters
    public int getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public double getPrice() { return price; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }
    
    // Setters
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setPrice(double price) { this.price = price; }
    public void setStatus(String status) { this.status = status; }
    public void setDescription(String description) { this.description = description; }
    
    @Override
    public String toString() {
        return "Room #" + roomNumber + " (" + roomType + ") - " + status;
    }
}