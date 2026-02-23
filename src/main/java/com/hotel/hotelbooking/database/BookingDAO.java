package com.hotel.hotelbooking.database;

import com.hotel.hotelbooking.model.Booking;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {
    
    /**
     * Get all bookings
     */
    public static List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, c.full_name, r.room_number " +
                       "FROM bookings b " +
                       "JOIN customers c ON b.customer_id = c.customer_id " +
                       "JOIN rooms r ON b.room_id = r.room_id";
        
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                
                while (rs.next()) {
                    Booking booking = new Booking(
                        rs.getInt("booking_id"),
                        rs.getInt("customer_id"),
                        rs.getInt("room_id"),
                        rs.getDate("checkin_date").toLocalDate(),
                        rs.getDate("checkout_date").toLocalDate(),
                        rs.getDouble("total_price"),
                        rs.getString("status")
                    );
                    booking.setCustomerName(rs.getString("full_name"));
                    booking.setRoomNumber(rs.getString("room_number"));
                    bookings.add(booking);
                }
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookings;
    }
    
    /**
     * Get bookings by status
     */
    public static List<Booking> getBookingsByStatus(String status) {
        List<Booking> bookings = new ArrayList<>();
        String query = "SELECT b.*, c.full_name, r.room_number " +
                       "FROM bookings b " +
                       "JOIN customers c ON b.customer_id = c.customer_id " +
                       "JOIN rooms r ON b.room_id = r.room_id " +
                       "WHERE b.status = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Booking booking = new Booking(
                    rs.getInt("booking_id"),
                    rs.getInt("customer_id"),
                    rs.getInt("room_id"),
                    rs.getDate("checkin_date").toLocalDate(),
                    rs.getDate("checkout_date").toLocalDate(),
                    rs.getDouble("total_price"),
                    rs.getString("status")
                );
                booking.setCustomerName(rs.getString("full_name"));
                booking.setRoomNumber(rs.getString("room_number"));
                bookings.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookings;
    }
    
    /**
     * Create new booking
     */
    public static boolean createBooking(Booking booking) {
        String query = "INSERT INTO bookings (customer_id, room_id, checkin_date, checkout_date, total_price, status) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, booking.getCustomerId());
            pstmt.setInt(2, booking.getRoomId());
            pstmt.setDate(3, Date.valueOf(booking.getCheckinDate()));
            pstmt.setDate(4, Date.valueOf(booking.getCheckoutDate()));
            pstmt.setDouble(5, booking.getTotalPrice());
            pstmt.setString(6, booking.getStatus());
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update booking
     */
    public static boolean updateBooking(Booking booking) {
        String query = "UPDATE bookings SET customer_id=?, room_id=?, checkin_date=?, " +
                       "checkout_date=?, total_price=?, status=? WHERE booking_id=?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, booking.getCustomerId());
            pstmt.setInt(2, booking.getRoomId());
            pstmt.setDate(3, Date.valueOf(booking.getCheckinDate()));
            pstmt.setDate(4, Date.valueOf(booking.getCheckoutDate()));
            pstmt.setDouble(5, booking.getTotalPrice());
            pstmt.setString(6, booking.getStatus());
            pstmt.setInt(7, booking.getBookingId());
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete booking
     */
    public static boolean deleteBooking(int bookingId) {
        String query = "DELETE FROM bookings WHERE booking_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, bookingId);
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Get available rooms for date range (không có booking trong khoảng này)
     */
    public static List<com.hotel.hotelbooking.model.Room> getAvailableRoomsForDates(LocalDate checkIn, LocalDate checkOut) {
        List<com.hotel.hotelbooking.model.Room> rooms = new ArrayList<>();
        String query = "SELECT r.* FROM rooms r " +
                       "WHERE r.status = 'Available' AND r.room_id NOT IN " +
                       "(SELECT room_id FROM bookings " +
                       "WHERE status != 'Cancelled' AND " +
                       "NOT (checkout_date <= ? OR checkin_date >= ?))";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setDate(1, Date.valueOf(checkIn));
            pstmt.setDate(2, Date.valueOf(checkOut));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                com.hotel.hotelbooking.model.Room room = new com.hotel.hotelbooking.model.Room(
                    rs.getInt("room_id"),
                    rs.getString("room_number"),
                    rs.getString("room_type"),
                    rs.getDouble("price"),
                    rs.getString("status"),
                    rs.getString("description")
                );
                rooms.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }
}
