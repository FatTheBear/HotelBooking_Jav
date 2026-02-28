package com.hotel.hotelbooking.database;

import com.hotel.hotelbooking.model.Customer;
import com.hotel.hotelbooking.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public static void insert(Customer customer) throws Exception {
        String sql = "INSERT INTO customers(full_name, phone, email, id_card, address) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getIdCard());
            stmt.setString(5, customer.getAddress());

            stmt.executeUpdate();
        }
    }

    public static List<Customer> getAllCustomers() throws Exception {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Customer c = new Customer(
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("id_card"),
                        rs.getString("address")
                );
                list.add(c);
            }
        }

        return list;
    }

    public static void update(Customer customer) throws Exception {
        String sql = "UPDATE customers SET full_name=?, phone=?, email=?, id_card=?, address=? WHERE customer_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, customer.getFullName());
            stmt.setString(2, customer.getPhone());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getIdCard());
            stmt.setString(5, customer.getAddress());
            stmt.setInt(6, customer.getCustomerId());

            stmt.executeUpdate();
        }
    }

    public static void delete(int id) throws Exception {
        String sql = "DELETE FROM customers WHERE customer_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}