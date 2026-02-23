package com.hotel.hotelbooking.test;

import com.hotel.hotelbooking.database.DatabaseConnection;
import com.hotel.hotelbooking.database.CustomerDAO;
import com.hotel.hotelbooking.database.RoomDAO;
import com.hotel.hotelbooking.database.BookingDAO;
import com.hotel.hotelbooking.model.Customer;
import com.hotel.hotelbooking.model.Room;
import com.hotel.hotelbooking.model.Booking;

import java.sql.Connection;
import java.util.List;

public class TestDatabaseConnection {
    
    public static void main(String[] args) {
        System.out.println("=== TEST DATABASE CONNECTION ===\n");
        
        // Test 1: Connection
        testConnection();
        
        // Test 2: Query customers
        testCustomers();
        
        // Test 3: Query rooms
        testRooms();
        
        // Test 4: Query bookings
        testBookings();
    }
    
    private static void testConnection() {
        System.out.println("1️⃣  Testing Database Connection...");
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Connection successful!");
                System.out.println("   Database URL: jdbc:mysql://localhost:3306/hotel_booking");
                System.out.println();
            } else {
                System.out.println("❌ Connection is null or closed!");
            }
        } catch (Exception e) {
            System.out.println("❌ Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testCustomers() {
        System.out.println("2️⃣  Testing Customer Query...");
        try {
            List<Customer> customers = CustomerDAO.getAllCustomers();
            System.out.println("✅ Query successful!");
            System.out.println("   Total customers: " + customers.size());
            for (Customer c : customers) {
                System.out.println("   - " + c.getFullName() + " (" + c.getPhone() + ")");
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("❌ Query failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testRooms() {
        System.out.println("3️⃣  Testing Room Query...");
        try {
            List<Room> rooms = RoomDAO.getAllRooms();
            System.out.println("✅ Query successful!");
            System.out.println("   Total rooms: " + rooms.size());
            for (Room r : rooms) {
                System.out.println("   - Room #" + r.getRoomNumber() + " (" + r.getRoomType() + ") - " + r.getStatus());
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("❌ Query failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testBookings() {
        System.out.println("4️⃣  Testing Booking Query...");
        try {
            List<Booking> bookings = BookingDAO.getAllBookings();
            System.out.println("✅ Query successful!");
            System.out.println("   Total bookings: " + bookings.size());
            for (Booking b : bookings) {
                System.out.println("   - Booking #" + b.getBookingId() + " - " + b.getCustomerName() + " (Status: " + b.getStatus() + ")");
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("❌ Query failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
