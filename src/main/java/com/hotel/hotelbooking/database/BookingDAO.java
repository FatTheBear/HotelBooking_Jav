package com.hotel.hotelbooking.database;

import com.hotel.hotelbooking.model.Booking;
import com.hotel.hotelbooking.model.Room;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    // ─── SQL Queries ────────────────────────────────────────────────────────────

    private static final String SQL_GET_ALL =
        "SELECT b.*, c.full_name, r.room_number " +
        "FROM bookings b " +
        "JOIN customers c ON b.customer_id = c.customer_id " +
        "JOIN rooms r ON b.room_id = r.room_id " +
        "ORDER BY b.booking_id DESC";

    private static final String SQL_GET_BY_STATUS =
        "SELECT b.*, c.full_name, r.room_number " +
        "FROM bookings b " +
        "JOIN customers c ON b.customer_id = c.customer_id " +
        "JOIN rooms r ON b.room_id = r.room_id " +
        "WHERE b.status = ? " +
        "ORDER BY b.booking_id DESC";

    private static final String SQL_CREATE =
        "INSERT INTO bookings (customer_id, room_id, checkin_date, checkout_date, total_price, status) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE bookings SET customer_id=?, room_id=?, checkin_date=?, " +
        "checkout_date=?, total_price=?, status=? WHERE booking_id=?";

    private static final String SQL_DELETE =
        "DELETE FROM bookings WHERE booking_id = ?";

    /** For CREATE: exclude all non-cancelled bookings in the date range. */
    private static final String SQL_AVAILABLE_ROOMS =
        "SELECT r.* FROM rooms r " +
        "WHERE r.status = 'Available' AND r.room_id NOT IN (" +
        "  SELECT room_id FROM bookings " +
        "  WHERE status != 'Cancelled' " +
        "  AND NOT (checkout_date <= ? OR checkin_date >= ?)" +
        ")";

    /** For UPDATE: same as above but exclude the booking being edited (so its own room stays visible). */
    private static final String SQL_AVAILABLE_ROOMS_EXCLUDE =
        "SELECT r.* FROM rooms r " +
        "WHERE r.status = 'Available' AND r.room_id NOT IN (" +
        "  SELECT room_id FROM bookings " +
        "  WHERE status != 'Cancelled' " +
        "  AND booking_id != ? " +
        "  AND NOT (checkout_date <= ? OR checkin_date >= ?)" +
        ")";

    /** Check if a room has any conflicting booking (excluding a specific booking ID, 0 = no exclusion). */
    private static final String SQL_HAS_CONFLICT =
        "SELECT COUNT(*) FROM bookings " +
        "WHERE room_id = ? " +
        "AND status != 'Cancelled' " +
        "AND booking_id != ? " +
        "AND NOT (checkout_date <= ? OR checkin_date >= ?)";

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /**
     * Map a ResultSet row to a Booking object (with customerName and roomNumber).
     */
    private static Booking mapRow(ResultSet rs) throws SQLException {
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
        return booking;
    }

    // ─── Public API ──────────────────────────────────────────────────────────────

    /**
     * Get all bookings ordered by booking_id DESC.
     */
    public static List<Booking> getAllBookings() {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_GET_ALL)) {

            while (rs.next()) {
                bookings.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("getAllBookings error: " + e.getMessage());
        }
        return bookings;
    }

    /**
     * Get bookings filtered by status.
     */
    public static List<Booking> getBookingsByStatus(String status) {
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_GET_BY_STATUS)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getBookingsByStatus error: " + e.getMessage());
        }
        return bookings;
    }

    /**
     * Create a new booking. Returns true on success.
     */
    public static boolean createBooking(Booking booking) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_CREATE)) {

            pstmt.setInt(1, booking.getCustomerId());
            pstmt.setInt(2, booking.getRoomId());
            pstmt.setDate(3, Date.valueOf(booking.getCheckinDate()));
            pstmt.setDate(4, Date.valueOf(booking.getCheckoutDate()));
            pstmt.setDouble(5, booking.getTotalPrice());
            pstmt.setString(6, booking.getStatus());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("createBooking error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update an existing booking. Returns true on success.
     */
    public static boolean updateBooking(Booking booking) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE)) {

            pstmt.setInt(1, booking.getCustomerId());
            pstmt.setInt(2, booking.getRoomId());
            pstmt.setDate(3, Date.valueOf(booking.getCheckinDate()));
            pstmt.setDate(4, Date.valueOf(booking.getCheckoutDate()));
            pstmt.setDouble(5, booking.getTotalPrice());
            pstmt.setString(6, booking.getStatus());
            pstmt.setInt(7, booking.getBookingId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateBooking error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete a booking by ID. Returns true on success.
     */
    public static boolean deleteBooking(int bookingId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE)) {

            pstmt.setInt(1, bookingId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("deleteBooking error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get rooms available for a date range (used when CREATING a new booking).
     * Excludes rooms that have any non-Cancelled booking overlapping the range.
     */
    public static List<Room> getAvailableRoomsForDates(LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_AVAILABLE_ROOMS)) {

            pstmt.setDate(1, Date.valueOf(checkIn));
            pstmt.setDate(2, Date.valueOf(checkOut));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRoom(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getAvailableRoomsForDates error: " + e.getMessage());
        }
        return rooms;
    }

    /**
     * Get rooms available for a date range when EDITING an existing booking.
     * Excludes conflicting bookings but ignores the booking being edited
     * (so its current room remains selectable).
     *
     * @param excludeBookingId the booking_id to exclude from conflict check
     */
    public static List<Room> getAvailableRoomsForDates(LocalDate checkIn, LocalDate checkOut, int excludeBookingId) {
        List<Room> rooms = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_AVAILABLE_ROOMS_EXCLUDE)) {

            pstmt.setInt(1, excludeBookingId);
            pstmt.setDate(2, Date.valueOf(checkIn));
            pstmt.setDate(3, Date.valueOf(checkOut));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapRoom(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("getAvailableRoomsForDates(exclude) error: " + e.getMessage());
        }
        return rooms;
    }

    /**
     * Check whether a room has any conflicting booking in the given date range.
     * Used as a server-side guard before saving to prevent race conditions.
     *
     * @param roomId           the room to check
     * @param checkIn          desired check-in date
     * @param checkOut         desired check-out date
     * @param excludeBookingId booking to exclude (pass 0 for new bookings)
     * @return true if a conflict exists
     */
    public static boolean hasConflict(int roomId, LocalDate checkIn, LocalDate checkOut, int excludeBookingId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_HAS_CONFLICT)) {

            pstmt.setInt(1, roomId);
            pstmt.setInt(2, excludeBookingId);
            pstmt.setDate(3, Date.valueOf(checkIn));
            pstmt.setDate(4, Date.valueOf(checkOut));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("hasConflict error: " + e.getMessage());
        }
        return false;
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────────

    // ─── Private Helpers ─────────────────────────────────────────────────────────

    private static Room mapRoom(ResultSet rs) throws SQLException {
        Room room = new Room(
                rs.getInt("room_id"),
                rs.getString("room_number"),
                rs.getString("room_type"),
                rs.getDouble("price"),
                rs.getInt("capacity"), // <-- FIX: lấy capacity từ DB
                rs.getString("status"),
                rs.getString("description")
        );

        // Optional: nếu bảng rooms có cột floor thì set thêm (không có thì xoá 3 dòng này)
        try {
            room.setFloor(rs.getInt("floor"));
        } catch (SQLException ignored) {
            // column 'floor' not present in result set -> ignore
        }

        return room;
    }
}
