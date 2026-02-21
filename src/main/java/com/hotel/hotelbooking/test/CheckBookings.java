package com.hotel.hotelbooking.test;

import com.hotel.hotelbooking.database.BookingDAO;
import com.hotel.hotelbooking.model.Booking;
import java.util.List;

public class CheckBookings {
    public static void main(String[] args) {
        System.out.println("\n=== CHECKING BOOKINGS DATA ===\n");
        
        try {
            List<Booking> bookings = BookingDAO.getAllBookings();
            System.out.println("Total bookings: " + bookings.size());
            
            if (bookings.isEmpty()) {
                System.out.println("\n⚠️  No bookings found in database!");
                System.out.println("Need to insert test booking data.\n");
            } else {
                System.out.println("\n✅ Bookings found:\n");
                for (Booking b : bookings) {
                    System.out.println("  ID: " + b.getBookingId() + 
                                     " | Customer: " + b.getCustomerName() + 
                                     " | Room: " + b.getRoomNumber() + 
                                     " | Status: " + b.getStatus());
                }
                System.out.println();
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
