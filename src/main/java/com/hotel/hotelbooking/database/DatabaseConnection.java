package com.hotel.hotelbooking.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    
    // Database configuration
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_booking";
    private static final String USER = "root";
    private static final String PASSWORD = "";  // XAMPP default: empty password
    
    private static Connection connection = null;
    
    /**
     * Get database connection (Singleton pattern)
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Maven already has MySQL driver, no need for Class.forName
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Database connected successfully!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
    
    /**
     * Close database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Test connection (for development)
     */
    public static void main(String[] args) {
        System.out.println("🔄 Testing database connection...");
        Connection conn = getConnection();
        
        if (conn != null) {
            System.out.println("🎉 TEST SUCCESSFUL!");
            System.out.println("Database: hotel_booking_db");
            System.out.println("Connection: " + conn);
            closeConnection();
        } else {
            System.out.println("❌ TEST FAILED!");
            System.out.println("Please check:");
            System.out.println("1. XAMPP MySQL is running");
            System.out.println("2. Database 'hotel_booking_db' exists");
            System.out.println("3. Username/Password is correct");
        }
    }
}