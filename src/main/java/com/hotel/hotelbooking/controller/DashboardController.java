package com.hotel.hotelbooking.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

public class DashboardController {
    
    @FXML
    private Button btnRooms;
    
    @FXML
    private Button btnCustomers;
    
    @FXML
    private Button btnBookings;
    
    @FXML
    private Button btnExit;
    
    @FXML
    private void handleRooms(ActionEvent event) {
        showAlert("Rooms Management", "Room management feature coming soon!\n(Will be implemented by Hop)");
    }
    
    @FXML
    private void handleCustomers(ActionEvent event) {
        showAlert("Customers Management", "Customer management feature coming soon!\n(Will be implemented by Sang)");
    }
    
    @FXML
    private void handleBookings(ActionEvent event) {
        try {
            com.hotel.hotelbooking.App.setRoot("booking");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open Bookings module: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleExit(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("Are you sure you want to exit?");
        alert.setContentText("Click OK to exit the application.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            Stage stage = (Stage) btnExit.getScene().getWindow();
            stage.close();
            System.out.println("Application closed by user.");
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}