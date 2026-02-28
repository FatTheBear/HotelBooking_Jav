package com.hotel.hotelbooking.controller;

import java.io.IOException;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;


public class DashboardController {
    
    
    
    @FXML
    private Button btnRooms;
    
    @FXML private AnchorPane contentArea;
    
    @FXML
    private Button btnCustomers;
    
    @FXML
    private Button btnBookings;
    
    @FXML
    private Button btnChatBot;
    
    @FXML
    private Button btnExit;
    
    
    @FXML
    private VBox contentBox;

    @FXML
    public void initialize() {
        generateRoomCards();
    }
    
    
//    @FXML
//    private void handleCustomers(ActionEvent event) {
//        showAlert("Customers Management", "Customer management feature coming soon!\n(Will be implemented by Sang)");
//    }
    @FXML
    private void handleRooms(ActionEvent event) {
        System.out.println("Rooms clicked");

        try {
            URL url = getClass().getResource("/com/hotel/hotelbooking/room.fxml");
            System.out.println("room.fxml url = " + url);

            Parent view = FXMLLoader.load(url);

            contentBox.getChildren().setAll(view);
            System.out.println("Room UI loaded OK");
        } catch (Exception e) {
            System.out.println("FAILED to load Room UI: " + e);
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCustomers(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/hotel/hotelbooking/CustomerView.fxml"));

            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Customer Management");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
    private void handleChatBot(ActionEvent event) {
        try {
            com.hotel.hotelbooking.App.setRoot("chatbot");
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open ChatBot: " + e.getMessage());
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
    private void generateRoomCards() {

    HBox roomContainer = new HBox(20);
    roomContainer.setAlignment(Pos.CENTER);

    String[] roomNames = {
            "Standard Room",
            "Deluxe Room",
            "Family Room",
            "Suite Room"
    };

    String[] prices = {
            "500.000VND / night",
            "1.000.000VND / night",
            "1.500.000VND / night",
            "2.000.000VND / night"
    };

    for (int i = 0; i < 4; i++) {

        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-padding: 15;" +
                "-fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );

        ImageView imageView = new ImageView(
                new Image(getClass().getResourceAsStream("/images/room" + (i+1) + ".jpg"))
        );
        imageView.setFitWidth(180);
        imageView.setFitHeight(120);
        imageView.setPreserveRatio(true);

        Label name = new Label(roomNames[i]);
        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label price = new Label(prices[i]);
        price.setStyle("-fx-text-fill: #3498db;");

        Button bookBtn = new Button("Book Now");
        bookBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        card.getChildren().addAll(imageView, name, price, bookBtn);
        roomContainer.getChildren().add(card);
    }

    contentBox.getChildren().add(roomContainer);
}
}

