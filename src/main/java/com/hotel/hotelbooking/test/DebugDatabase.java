package com.hotel.hotelbooking.test;

import com.hotel.hotelbooking.database.DatabaseConnection;
import com.hotel.hotelbooking.database.CustomerDAO;
import com.hotel.hotelbooking.database.RoomDAO;
import com.hotel.hotelbooking.model.Customer;
import com.hotel.hotelbooking.model.Room;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.List;

public class DebugDatabase {
    
    public static void main(String[] args) {
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║         DATABASE CONNECTION & DATA DEBUG             ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
        
        // Test 1: Connection
        System.out.println("1️⃣  TESTING CONNECTION...");
        Connection conn = testConnection();
        
        if (conn != null) {
            // Test 2: Database metadata
            System.out.println("\n2️⃣  CHECKING DATABASE METADATA...");
            checkDatabaseMetadata(conn);
            
            // Test 3: Check if tables exist
            System.out.println("\n3️⃣  CHECKING TABLES...");
            checkTables(conn);
            
            // Test 4: Query data
            System.out.println("\n4️⃣  QUERYING DATA FROM TABLES...");
            testQueries();
        }
        
        System.out.println("\n╔══════════════════════════════════════════════════════╗");
        System.out.println("║                  DEBUG COMPLETE                     ║");
        System.out.println("╚══════════════════════════════════════════════════════╝\n");
    }
    
    private static Connection testConnection() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("   ✅ Connection: SUCCESS");
                System.out.println("   ✅ URL: jdbc:mysql://localhost:3306/hotel_booking");
                System.out.println("   ✅ User: root");
                System.out.println("   ✅ Status: ACTIVE\n");
                return conn;
            } else {
                System.out.println("   ❌ Connection is null or closed!\n");
                return null;
            }
        } catch (Exception e) {
            System.out.println("   ❌ CONNECTION FAILED!");
            System.out.println("   Error: " + e.getMessage());
            System.out.println("   Cause: " + e.getCause());
            System.out.println("\n   🔍 Possible causes:");
            System.out.println("      • MySQL is not running");
            System.out.println("      • Database 'hotel_booking' does not exist");
            System.out.println("      • Wrong username/password\n");
            e.printStackTrace();
            return null;
        }
    }
    
    private static void checkDatabaseMetadata(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("   ✅ Database: " + meta.getDatabaseProductName());
            System.out.println("   ✅ Version: " + meta.getDatabaseProductVersion());
            System.out.println("   ✅ Driver: " + meta.getDriverName() + " " + meta.getDriverVersion() + "\n");
        } catch (Exception e) {
            System.out.println("   ❌ Error getting metadata: " + e.getMessage() + "\n");
        }
    }
    
    private static void checkTables(Connection conn) {
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "%", new String[]{"TABLE"});
            
            System.out.println("   Tables in database:\n");
            boolean hasCustomer = false, hasRoom = false, hasBooking = false;
            
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                System.out.println("      • " + tableName);
                
                if (tableName.equalsIgnoreCase("customer")) hasCustomer = true;
                if (tableName.equalsIgnoreCase("room")) hasRoom = true;
                if (tableName.equalsIgnoreCase("booking")) hasBooking = true;
            }
            
            System.out.println();
            if (!hasCustomer) System.out.println("   ⚠️  WARNING: 'customer' table not found!");
            if (!hasRoom) System.out.println("   ⚠️  WARNING: 'room' table not found!");
            if (!hasBooking) System.out.println("   ⚠️  WARNING: 'booking' table not found!");
            
            if (hasCustomer && hasRoom && hasBooking) {
                System.out.println("   ✅ All required tables exist!\n");
            } else {
                System.out.println();
            }
            
        } catch (Exception e) {
            System.out.println("   ❌ Error checking tables: " + e.getMessage() + "\n");
        }
    }
    
    private static void testQueries() {
        System.out.println();
        
        // Test Customer
        System.out.println("   📍 CUSTOMER TABLE:");
        try {
            List<Customer> customers = CustomerDAO.getAllCustomers();
            if (customers.isEmpty()) {
                System.out.println("      ⚠️  No data found in customer table!");
            } else {
                System.out.println("      ✅ Records found: " + customers.size());
                for (Customer c : customers) {
                    System.out.println("         - ID: " + c.getCustomerId() + " | Name: " + c.getFullName() + " | Phone: " + c.getPhone());
                }
            }
        } catch (Exception e) {
            System.out.println("      ❌ Query failed: " + e.getMessage());
        }
        
        System.out.println();
        
        // Test Room
        System.out.println("   📍 ROOM TABLE:");
        try {
            List<Room> rooms = RoomDAO.getAllRooms();
            if (rooms.isEmpty()) {
                System.out.println("      ⚠️  No data found in room table!");
            } else {
                System.out.println("      ✅ Records found: " + rooms.size());
                for (Room r : rooms) {
                    System.out.println("         - ID: " + r.getRoomId() + " | Room: " + r.getRoomNumber() + " | Type: " + r.getRoomType() + " | Price: " + r.getPrice() + " | Status: " + r.getStatus());
                }
            }
        } catch (Exception e) {
            System.out.println("      ❌ Query failed: " + e.getMessage());
        }
        
        System.out.println();
    }
}
