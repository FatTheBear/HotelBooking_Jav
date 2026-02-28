/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.hotel.hotelbooking.controller;

import com.hotel.hotelbooking.model.Room;
import com.hotel.hotelbooking.database.RoomDAO; 
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * RoomController (Room Management screen)
 *
 * Responsibilities:
 * 1) Load rooms from DB (RoomDAO) and show them in a TableView.
 * 2) Provide CRUD actions via the form (Create/Update/Delete/Reset).
 * 3) When selecting a row in the table, populate the form for editing.
 *
 * NOTE:
 * - This controller assumes your room.id matching these fields.
 * - This controller uses RoomDAO.getAllRooms() / getAvailableRooms() that you already updated to include capacity.
 *
 * TODO (later):
 * - Add RoomDAO.createRoom/updateRoom/deleteRoom methods if you don't have them yet.
 */
public class RoomController implements Initializable {

    // ===== Form fields (left side) =====
    @FXML private TextField txtRoomNumber;
    @FXML private ComboBox<String> cboRoomType;
    @FXML private TextField txtPrice;
    @FXML private Spinner<Integer> spnCapacity;
    @FXML private ComboBox<String> cboStatus;
    @FXML private TextArea txtDescription;

    // Buttons
    @FXML private Button btnCreate;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnReset;

    // Optional search
    @FXML private TextField txtSearch;

    // ===== Table (center) =====
    @FXML private TableView<Room> tblRooms;
    @FXML private TableColumn<Room, Integer> colId;
    @FXML private TableColumn<Room, String> colNumber;
    @FXML private TableColumn<Room, String> colType;
    @FXML private TableColumn<Room, Double> colPrice;
    @FXML private TableColumn<Room, Integer> colCapacity;
    @FXML private TableColumn<Room, String> colStatus;
    @FXML private TableColumn<Room, String> colDesc;

    @FXML private Label lblHint;

    // Local state
    private ObservableList<Room> roomList = FXCollections.observableArrayList();
    private Room selectedRoom = null;
    

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupCombos();
        setupCapacitySpinner();
        setupTableColumns();
        setupTableSelection();

        loadRooms();
        resetFormState();
    }

    // ===== Setup helpers =====

    private void setupCombos() {
        // You can change these values to match your database conventions.
        cboRoomType.setItems(FXCollections.observableArrayList(
            "Single", "Double", "Twin", "Suite", "VIP"
        ));
        cboRoomType.setValue("Single");

        cboStatus.setItems(FXCollections.observableArrayList(
            "Available", "Occupied", "Maintenance"
        ));
        cboStatus.setValue("Available");
    }

    private void setupCapacitySpinner() {
        // Min=1, Max=20, Initial=2 (you can adjust max later)
        SpinnerValueFactory<Integer> vf = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2);
        spnCapacity.setValueFactory(vf);
        spnCapacity.setEditable(true);

        // Make spinner text field accept only integers (basic UX improvement)
        spnCapacity.getEditor().setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) return change;
            return null;
        }));
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(cd ->
            new SimpleIntegerProperty(cd.getValue().getRoomId()).asObject());

        colNumber.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getRoomNumber()));

        colType.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getRoomType()));

        colPrice.setCellValueFactory(cd ->
            new SimpleDoubleProperty(cd.getValue().getPrice()).asObject());

        colCapacity.setCellValueFactory(cd ->
            new SimpleIntegerProperty(cd.getValue().getCapacity()).asObject());

        colStatus.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getStatus()));

        colDesc.setCellValueFactory(cd ->
            new SimpleStringProperty(cd.getValue().getDescription()));

        // Optional: format price as currency-like text (simple)
        colPrice.setCellFactory(col -> new TableCell<Room, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%,.0f VND", item));
            }
        });
    }

    private void setupTableSelection() {
        tblRooms.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            selectedRoom = newVal;
            fillFormFromSelectedRoom(newVal);

            // Switch buttons to edit mode
            btnUpdate.setDisable(false);
            btnDelete.setDisable(false);
            btnCreate.setDisable(true);
        });
    }

    // ===== Data loading =====

    private void loadRooms() {
        List<Room> rooms = RoomDAO.getAllRooms();
        roomList = FXCollections.observableArrayList(rooms);
        tblRooms.setItems(roomList);
    }

    // ===== Form helpers =====

    private void fillFormFromSelectedRoom(Room room) {
        txtRoomNumber.setText(room.getRoomNumber());
        cboRoomType.setValue(room.getRoomType());
        txtPrice.setText(String.valueOf(room.getPrice()));
        spnCapacity.getValueFactory().setValue(room.getCapacity());
        cboStatus.setValue(room.getStatus());
        txtDescription.setText(room.getDescription() == null ? "" : room.getDescription());
        lblHint.setText("Editing Room ID: " + room.getRoomId());
    }

    private void resetFormState() {
        selectedRoom = null;
        txtRoomNumber.clear();
        cboRoomType.setValue("Single");
        txtPrice.clear();
        spnCapacity.getValueFactory().setValue(2);
        cboStatus.setValue("Available");
        txtDescription.clear();

        tblRooms.getSelectionModel().clearSelection();

        // Switch buttons back to create mode
        btnCreate.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        lblHint.setText("Tip: Select a row to edit.");
    }

    private Room buildRoomFromFormOrNull() {
        String number = txtRoomNumber.getText() == null ? "" : txtRoomNumber.getText().trim();
        String type = cboRoomType.getValue();
        String status = cboStatus.getValue();
        String desc = txtDescription.getText();

        if (number.isEmpty() || type == null || status == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error",
                "Please fill in Room Number, Type, and Status.");
            return null;
        }

        double price;
        try {
            price = Double.parseDouble(txtPrice.getText().trim());
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Price must be a number.");
            return null;
        }
        if (price < 0) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Price must be >= 0.");
            return null;
        }

        int capacity = spnCapacity.getValue();
        if (capacity < 1) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Capacity must be >= 1.");
            return null;
        }

        // If creating: roomId = 0 (or ignored by DB if AUTO_INCREMENT)
        int roomId = (selectedRoom == null) ? 0 : selectedRoom.getRoomId();

        return new Room(roomId, number, type, price, capacity, status, desc);
    }

    // ===== Button handlers =====

    @FXML
private void handleCreate(ActionEvent event) {
    Room room = buildRoomFromFormOrNull();
    if (room == null) return;

    boolean ok = RoomDAO.createRoom(room);
    if (ok) {
        showAlert(Alert.AlertType.INFORMATION, "Success", "Room created successfully!");
        loadRooms();          // refresh TableView data
        resetFormState();     // clear form + back to Create mode
    } else {
        showAlert(Alert.AlertType.ERROR, "Error",
            "Failed to create room.\n" +
            "Tip: If room_number must be unique, please check duplicates.");
    }
}

    @FXML
    private void handleUpdate(ActionEvent event) {
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a room from the table first.");
            return;
        }

        Room updated = buildRoomFromFormOrNull();
        if (updated == null) return;

        // TODO: requires you to implement RoomDAO.updateRoom(updated)
        // boolean ok = RoomDAO.updateRoom(updated);

        showAlert(Alert.AlertType.INFORMATION, "TODO",
            "Update is not implemented yet.\nNext step: add RoomDAO.updateRoom(room) method.");
    }

    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a room from the table first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Room #" + selectedRoom.getRoomNumber());
        confirm.setContentText("Are you sure you want to delete this room? This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        // TODO: requires you to implement RoomDAO.deleteRoom(selectedRoom.getRoomId())
        // boolean ok = RoomDAO.deleteRoom(selectedRoom.getRoomId());

        showAlert(Alert.AlertType.INFORMATION, "TODO",
            "Delete is not implemented yet.\nNext step: add RoomDAO.deleteRoom(roomId) method.");
    }

    @FXML
    private void handleReset(ActionEvent event) {
        resetFormState();
    }

    // ===== Utilities =====

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}