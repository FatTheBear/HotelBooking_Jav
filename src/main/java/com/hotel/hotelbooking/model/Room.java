package com.hotel.hotelbooking.model;


/**
 * Room model class.
 *
 * This represents one row in the `rooms` table.
 * You already added a new DB column: `capacity`,
 * so the model must include it to display/edit it in the Room UI
 * and later validate Booking.num_guests Room.capacity*/
public class Room {
    private int roomId;
    private String roomNumber;
    private String roomType;
    private double price;

    // NEW: maximum number of guests allowed in this room
    private int capacity;

    private String status;
    private String description;

    /**
     * Full constructor (recommended).
     * Use this when loading Room from DB.
     */
    public Room(int roomId, String roomNumber, String roomType,
                double price, int capacity, String status, String description) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.price = price;
        this.capacity = capacity;
        this.status = status;
        this.description = description;
    }

    /**
     * Backward-compatible constructor (without capacity).
     * Kept so your existing code does NOT break immediately.
     * We set a reasonable default capacity (2).
     *
     * Later, when you finish updating all DAOs/controllers,
     * you can remove this constructor if you want.
     */
    public Room(int roomId, String roomNumber, String roomType,
                double price, String status, String description) {
        this(roomId, roomNumber, roomType, price, 2, status, description);
    }

    /** Default constructor (used by some frameworks / manual setters). */
    public Room() {}

    // Getters
    public int getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public double getPrice() { return price; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }
    public String getDescription() { return description; }

    // Setters
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setPrice(double price) { this.price = price; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setStatus(String status) { this.status = status; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        // This string is shown when Room objects are displayed in a ComboBox.
        // Keeping it human-readable helps during demo.
        return "Room #" + roomNumber + " (" + roomType + ", cap " + capacity + ") - " + status;
    }
}