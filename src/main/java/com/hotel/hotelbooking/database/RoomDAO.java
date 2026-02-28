package com.hotel.hotelbooking.database;

import com.hotel.hotelbooking.database.DatabaseConnection;
import com.hotel.hotelbooking.model.Room;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomDAO
 *
 * Handles all database operations for table: rooms
 *
 * Columns expected (based on your Room model + DB change):
 * - room_id (PK, usually AUTO_INCREMENT)
 * - room_number (should be UNIQUE)
 * - room_type
 * - price
 * - capacity
 * - status
 * - description
 */
public class RoomDAO {

    // -------------------------
    // Internal mapper (DRY)
    // -------------------------

    /**
     * Convert one ResultSet row into a Room object.
     * Keeping mapping in one place prevents mistakes when schema changes.
     */
    private static Room mapRowToRoom(ResultSet rs) throws SQLException {
        return new Room(
            rs.getInt("room_id"),
            rs.getString("room_number"),
            rs.getString("room_type"),
            rs.getDouble("price"),
            rs.getInt("capacity"),
            rs.getString("status"),
            rs.getString("description")
        );
    }

    // -------------------------
    // READ methods
    // -------------------------

    /** Get all rooms (used for Room Management table). */
    public static List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();

        // Explicit columns are safer than SELECT *
        String query =
            "SELECT room_id, room_number, room_type, price, capacity, status, description " +
            "FROM rooms";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    /** Get rooms with status = 'Available' (used for Booking room selection). */
    public static List<Room> getAvailableRooms() {
        List<Room> rooms = new ArrayList<>();

        String query =
            "SELECT room_id, room_number, room_type, price, capacity, status, description " +
            "FROM rooms " +
            "WHERE status = 'Available'";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                rooms.add(mapRowToRoom(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    /** Get a single room by its primary key. */
    public static Room getRoomById(int roomId) {
        String query =
            "SELECT room_id, room_number, room_type, price, capacity, status, description " +
            "FROM rooms " +
            "WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, roomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToRoom(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Helper read: capacity only.
    
     */
    public static int getCapacityByRoomId(int roomId) {
        String query = "SELECT capacity FROM rooms WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, roomId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("capacity");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    // -------------------------
    // CREATE / UPDATE / DELETE
    // -------------------------

    /**
     * CREATE: Insert a new room.
     *
     * @return true if insert succeeded
     */
    public static boolean createRoom(Room room) {
        String query =
            "INSERT INTO rooms (room_number, room_type, price, capacity, status, description) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, room.getRoomNumber());
            pstmt.setString(2, room.getRoomType());
            pstmt.setDouble(3, room.getPrice());
            pstmt.setInt(4, room.getCapacity());
            pstmt.setString(5, room.getStatus());
            pstmt.setString(6, room.getDescription());

            int rows = pstmt.executeUpdate();
            if (rows <= 0) return false;

            // Optional: read generated room_id and set it back into the model
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    room.setRoomId(keys.getInt(1));
                }
            }

            return true;

        } catch (SQLException e) {
            // Common error here: duplicate room_number if UNIQUE constraint exists
            e.printStackTrace();
            return false;
        }
    }

    /**
     * UPDATE: Update an existing room identified by room_id.
     *
     * @return true if update succeeded
     */
    public static boolean updateRoom(Room room) {
    String query =
        "UPDATE rooms " +
        "SET room_number = ?, room_type = ?, price = ?, capacity = ?, floor = ?, status = ?, description = ? " +
        "WHERE room_id = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {

        pstmt.setString(1, room.getRoomNumber());
        pstmt.setString(2, room.getRoomType());
        pstmt.setDouble(3, room.getPrice());
        pstmt.setInt(4, room.getCapacity());
        pstmt.setInt(5, room.getFloor());
        pstmt.setString(6, room.getStatus());
        pstmt.setString(7, room.getDescription());
        pstmt.setInt(8, room.getRoomId());

        return pstmt.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

    /**
     * DELETE: Delete a room by room_id.
     *
     * WARNING:
     * If you later add FK constraints from booking.room_id -> rooms.room_id,
     * this delete might fail when the room is referenced by bookings.
     *
     * In that case, a "soft delete" approach is recommended:
     * - update status = 'Maintenance' (or 'Inactive') instead of DELETE.
     */
    public static boolean deleteRoom(int roomId) {
        String query = "DELETE FROM rooms WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, roomId);

            int rows = pstmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // -------------------------
    // Optional convenience methods
    // -------------------------

    /**
     * OPTIONAL: "Soft delete" (safer than physical delete).
     * You can call this instead of deleteRoom(...) in the UI.
     */
    public static boolean setRoomStatus(int roomId, String status) {
        String query = "UPDATE rooms SET status = ? WHERE room_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, roomId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}