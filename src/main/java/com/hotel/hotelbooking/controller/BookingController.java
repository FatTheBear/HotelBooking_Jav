package com.hotel.hotelbooking.controller;

import com.hotel.hotelbooking.App;
import com.hotel.hotelbooking.database.BookingDAO;
import com.hotel.hotelbooking.database.CustomerDAO;
import com.hotel.hotelbooking.database.RoomDAO;
import com.hotel.hotelbooking.model.Booking;
import com.hotel.hotelbooking.model.Customer;
import com.hotel.hotelbooking.model.Room;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.input.MouseEvent;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BookingController {

    //Fields
    @FXML
    private ComboBox<Customer> cboCustomer;
    @FXML
    private ComboBox<Room> cboRoom;
    @FXML
    private DatePicker dpCheckinDate;
    @FXML
    private DatePicker dpCheckoutDate;
    @FXML
    private ComboBox<String> cboStatus;
    @FXML
    private Label lblRoomPrice;
    @FXML
    private Label lblTotalPrice;
    @FXML
    private Label lblDateWarning; // inline warning label

    // ─── Form Buttons
    // ─────────────────────────────────────────────────────────────
    @FXML
    private Button btnSave; // Create or Update
    @FXML
    private Button btnDelete; // Delete selected booking
    @FXML
    private Button btnReset; // Clear / new form
    @FXML
    private Button btnExportCSV;
    @FXML
    private Button btnBack;

    // ─── Filter Buttons
    // ───────────────────────────────────────────────────────────
    @FXML
    private Button btnShowAll;
    @FXML
    private Button btnShowConfirmed;
    @FXML
    private Button btnShowPending;
    @FXML
    private Button btnShowCancelled;

    // ─── Search Fields ───────────────────────────────────────────────────────────
    @FXML
    private TextField txtSearchCustomer;
    @FXML
    private DatePicker dpSearchCheckin;
    @FXML
    private DatePicker dpSearchCheckout;

    // ─── Table
    // ────────────────────────────────────────────────────────────────────
    @FXML
    private TableView<Booking> tblBooking;
    @FXML
    private TableColumn<Booking, Integer> colBookingId;
    @FXML
    private TableColumn<Booking, String> colCustomer;
    @FXML
    private TableColumn<Booking, String> colPhone;
    @FXML
    private TableColumn<Booking, String> colRoom;
    @FXML
    private TableColumn<Booking, LocalDate> colCheckin;
    @FXML
    private TableColumn<Booking, LocalDate> colCheckout;
    @FXML
    private TableColumn<Booking, Double> colTotalPrice;
    @FXML
    private TableColumn<Booking, String> colStatus;

    private ObservableList<Booking> bookingList;

    private Booking selectedBooking = null;

    private boolean isDirty = false;

    private boolean isLoading = false;

    private boolean isRevertingSelection = false;

    @FXML
    public void initialize() {
        isLoading = true;
        try {
            cboStatus.setItems(FXCollections.observableArrayList("Pending", "Confirmed", "Cancelled"));
            cboStatus.setValue("Pending");

            dpCheckinDate.setValue(LocalDate.now());
            dpCheckoutDate.setValue(LocalDate.now().plusDays(1));

            loadCustomers();
            loadRooms(dpCheckinDate.getValue(), dpCheckoutDate.getValue());
            loadBookings();
            setupTableColumns();
            setupTableSelection();
            setupDirtyListeners();

            calculateTotalPrice();
            btnDelete.setDisable(true);
        } finally {
            isLoading = false;
        }
    }

    // ─── Data Loading
    // ─────────────────────────────────────────────────────────────

    private void loadCustomers() {
        try {
            List<Customer> customers = CustomerDAO.getAllCustomers();
            cboCustomer.setItems(FXCollections.observableArrayList(customers));
            if (!customers.isEmpty()) {
                cboCustomer.setValue(customers.get(0));
            }
        } catch (Exception ex) {
            Logger.getLogger(BookingController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> rooms;
        if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
            if (selectedBooking != null) {
                // Update mode: exclude the current booking from conflict check
                rooms = BookingDAO.getAvailableRoomsForDates(checkIn, checkOut, selectedBooking.getBookingId());
            } else {
                // Create mode: standard check
                rooms = BookingDAO.getAvailableRoomsForDates(checkIn, checkOut);
            }
        } else {
            rooms = com.hotel.hotelbooking.database.RoomDAO.getAvailableRooms();
        }
        Room current = cboRoom.getValue();
        cboRoom.setItems(FXCollections.observableArrayList(rooms));
        // Restore selection if still available
        if (current != null && rooms.contains(current)) {
            cboRoom.setValue(current);
        } else if (!rooms.isEmpty()) {
            cboRoom.setValue(rooms.get(0));
        }
    }

    private void loadBookings() {
        List<Booking> bookings = BookingDAO.getAllBookings();
        bookingList = FXCollections.observableArrayList(bookings);
        tblBooking.setItems(bookingList);
    }

    // ─── Table Setup ─────────────────────────────────────────────────────────────

    private void setupTableColumns() {
        colBookingId.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getBookingId()).asObject());
        colCustomer.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getCustomerName()));
        colPhone.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getCustomerPhone()));
        colRoom.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getRoomNumber()));
        colCheckin.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getCheckinDate()));
        colCheckout.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getCheckoutDate()));
        colTotalPrice.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getTotalPrice()).asObject());
        colStatus.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStatus()));

        // Format currency
        colTotalPrice.setCellFactory(col -> new TableCell<Booking, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : formatCurrency(item));
            }
        });

    }

    private void setupTableSelection() {
        tblBooking.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (isRevertingSelection)
                return;
            if (isLoading) {
                isDirty = false;
                return;
            }
            if (newVal == null)
                return;

            if (isDirty && selectedBooking != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Unsaved Changes");
                confirm.setHeaderText("You have unsaved changes.");
                confirm.setContentText("Do you want to discard changes and load the selected booking?");
                ButtonType btnDiscard = new ButtonType("Discard & Load");
                ButtonType btnContinue = new ButtonType("Keep Editing", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirm.getButtonTypes().setAll(btnDiscard, btnContinue);

                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isEmpty() || result.get() != btnDiscard) {
                    final Booking revertTo = oldVal;
                    Platform.runLater(() -> {
                        isRevertingSelection = true;
                        try {
                            if (revertTo != null) {
                                tblBooking.getSelectionModel().select(revertTo);
                            } else {
                                tblBooking.getSelectionModel().clearSelection();
                            }
                        } finally {
                            isRevertingSelection = false;
                        }
                    });
                    return;
                }
                // "Discard & Load": refresh the table, then load the fresh version of the new
                // row
                loadBookings();
                // Find the refreshed booking object by ID (newVal may be stale after reload)
                final int targetId = newVal.getBookingId();
                bookingList.stream()
                        .filter(b -> b.getBookingId() == targetId)
                        .findFirst()
                        .ifPresent(this::loadBookingIntoForm);
                return;
            }

            loadBookingIntoForm(newVal);
        });
    }

    private void setupDirtyListeners() {
        cboCustomer.setOnAction(e -> {
            markDirty();
            calculateTotalPrice();
        });
        cboRoom.setOnAction(e -> {
            markDirty();
            calculateTotalPrice();
        });
        cboStatus.setOnAction(e -> markDirty());
        dpCheckinDate.setOnAction(e -> {
            markDirty();
            loadRooms(dpCheckinDate.getValue(), dpCheckoutDate.getValue());
            calculateTotalPrice();
        });
        dpCheckoutDate.setOnAction(e -> {
            markDirty();
            loadRooms(dpCheckinDate.getValue(), dpCheckoutDate.getValue());
            calculateTotalPrice();
        });
    }

    /**
     * Only marks the form dirty when the user actually changes something,
     * not when the form is being programmatically populated.
     */
    private void markDirty() {
        if (!isLoading) {
            isDirty = true;
        }
    }

    private void loadBookingIntoForm(Booking booking) {
        isLoading = true;
        try {
            selectedBooking = booking;
            isDirty = false;

            // Select matching customer
            cboCustomer.getItems().stream()
                    .filter(c -> c.getCustomerId() == booking.getCustomerId())
                    .findFirst()
                    .ifPresent(cboCustomer::setValue);

            dpCheckinDate.setValue(booking.getCheckinDate());
            dpCheckoutDate.setValue(booking.getCheckoutDate());
            cboStatus.setValue(booking.getStatus());

            // Reload rooms for these dates, then select the booked room
            loadRooms(booking.getCheckinDate(), booking.getCheckoutDate());
            cboRoom.getItems().stream()
                    .filter(r -> r.getRoomId() == booking.getRoomId())
                    .findFirst()
                    .ifPresentOrElse(
                            cboRoom::setValue,
                            () -> {
                                // Room may be unavailable (booked by others); add it temporarily
                                Room bookedRoom = com.hotel.hotelbooking.database.RoomDAO
                                        .getRoomById(booking.getRoomId());
                                if (bookedRoom != null) {
                                    cboRoom.getItems().add(0, bookedRoom);
                                    cboRoom.setValue(bookedRoom);
                                }
                            });

            calculateTotalPrice();
            btnDelete.setDisable(false);
            btnSave.setText("Update Booking");
            clearDateWarning();
        } finally {
            isLoading = false;
        }
    }

    // ─── Price Calculation ───────────────────────────────────────────────────────

    /**
     * Calculate total price and update UI labels.
     * 
     * @return the calculated total price, or 0 if invalid
     */
    private double calculateTotalPrice() {
        Room room = cboRoom.getValue();
        LocalDate checkIn = dpCheckinDate.getValue();
        LocalDate checkOut = dpCheckoutDate.getValue();

        if (room == null || checkIn == null || checkOut == null) {
            lblRoomPrice.setText("0 VND");
            lblTotalPrice.setText("0 VND");
            return 0;
        }

        if (!checkOut.isAfter(checkIn)) {
            showDateWarning("⚠ Check-out date must be after check-in date!");
            lblTotalPrice.setText("—");
            return 0;
        }

        clearDateWarning();
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double total = room.getPrice() * nights;
        lblRoomPrice.setText(formatCurrency(room.getPrice()));
        lblTotalPrice.setText(formatCurrency(total));
        return total;
    }

    private void showDateWarning(String msg) {
        lblDateWarning.setText(msg);
        lblDateWarning.setVisible(true);
    }

    private void clearDateWarning() {
        lblDateWarning.setText("");
        lblDateWarning.setVisible(false);
    }

    @FXML
    private void handleSave(ActionEvent event) {
        Customer customer = cboCustomer.getValue();
        Room room = cboRoom.getValue();
        LocalDate checkIn = dpCheckinDate.getValue();
        LocalDate checkOut = dpCheckoutDate.getValue();
        String status = cboStatus.getValue();

        if (customer == null || room == null || checkIn == null || checkOut == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all required fields.");
            return;
        }
        if (!checkOut.isAfter(checkIn)) {
            showDateWarning("⚠ Check-out date must be after check-in date!");
            return;
        }

        double totalPrice = calculateTotalPrice();

        // ── Server-side conflict check (guards against race conditions) ──
        int excludeId = (selectedBooking != null) ? selectedBooking.getBookingId() : 0;
        if (BookingDAO.hasConflict(room.getRoomId(), checkIn, checkOut, excludeId)) {
            showAlert(Alert.AlertType.WARNING, "Room Conflict",
                    "Room " + room.getRoomNumber() + " is already booked for the selected dates.\n" +
                            "Please choose a different room or adjust the dates.");
            // Refresh room list so the UI reflects the current state
            loadRooms(checkIn, checkOut);
            return;
        }

        if (selectedBooking == null) {
            // ── CREATE ──
            Booking booking = new Booking(0, customer.getCustomerId(), room.getRoomId(),
                    checkIn, checkOut, totalPrice, status);
            if (BookingDAO.createBooking(booking)) {
                // Auto-update room status when booking is Confirmed
                if ("Confirmed".equals(status)) {
                    RoomDAO.updateRoomStatus(room.getRoomId(), "Occupied");
                }
                showAlert(Alert.AlertType.INFORMATION, "Success", "Booking created successfully!");
                // Clear dirty state and set flags
                isLoading = true;
                isDirty = false;
                selectedBooking = null;

                handleReset(null);
                loadBookings();
                // Refresh room list to reflect status changes
                loadRooms(dpCheckinDate.getValue(), dpCheckoutDate.getValue());
                isLoading = false;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create booking.");
            }
        } else {
            // ── UPDATE ──
            // Store old room ID and status for status change handling
            int oldRoomId = selectedBooking.getRoomId();
            String oldStatus = selectedBooking.getStatus();

            selectedBooking.setCustomerId(customer.getCustomerId());
            selectedBooking.setRoomId(room.getRoomId());
            selectedBooking.setCheckinDate(checkIn);
            selectedBooking.setCheckoutDate(checkOut);
            selectedBooking.setTotalPrice(totalPrice);
            selectedBooking.setStatus(status);

            if (BookingDAO.updateBooking(selectedBooking)) {
                // Handle room status changes
                handleRoomStatusChange(oldRoomId, room.getRoomId(), oldStatus, status);

                showAlert(Alert.AlertType.INFORMATION, "Success", "Booking updated successfully!");
                // Clear dirty state FIRST, then set flag for safety

                isLoading = true;
                isDirty = false;
                selectedBooking = null;
                // Clear table selection to trigger listener
                tblBooking.getSelectionModel().clearSelection();
                // Reset form to clear dirty state
                handleReset(null);
                loadBookings();
                // Refresh room list to reflect status changes
                loadRooms(dpCheckinDate.getValue(), dpCheckoutDate.getValue());
                isLoading = false;
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update booking.");
            }
        }
    }

    /**
     * Handle room status changes based on booking status
     */
    private void handleRoomStatusChange(int oldRoomId, int newRoomId, String oldStatus, String newStatus) {
        // If booking is cancelled, make room available
        if ("Cancelled".equals(newStatus)) {
            RoomDAO.updateRoomStatus(newRoomId, "Available");
            return;
        }

        // If status changed from non-Confirmed to Confirmed
        if (!"Confirmed".equals(oldStatus) && "Confirmed".equals(newStatus)) {
            RoomDAO.updateRoomStatus(newRoomId, "Occupied");
        }

        // If status changed from Confirmed to non-Confirmed
        if ("Confirmed".equals(oldStatus) && !"Confirmed".equals(newStatus)) {
            RoomDAO.updateRoomStatus(oldRoomId, "Available");
        }

        // If room changed and new booking is Confirmed
        if (oldRoomId != newRoomId && "Confirmed".equals(newStatus)) {
            RoomDAO.updateRoomStatus(oldRoomId, "Available");
            RoomDAO.updateRoomStatus(newRoomId, "Occupied");
        }
    }

    /**
     * Delete the currently selected booking.
     */
    @FXML
    private void handleDelete(ActionEvent event) {
        if (selectedBooking == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a booking from the table first.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Booking #" + selectedBooking.getBookingId());
        confirm.setContentText("Are you sure you want to delete this booking? This action cannot be undone.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (BookingDAO.deleteBooking(selectedBooking.getBookingId())) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Booking deleted successfully!");
                handleReset(null);
                loadBookings();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete booking.");
            }
        }
    }

    @FXML
    private void handlePaneClick(MouseEvent event) {
    }

    @FXML
    private void handleReset(ActionEvent event) {
        isLoading = true;
        try {
            selectedBooking = null;
            isDirty = false;

            if (!cboCustomer.getItems().isEmpty())
                cboCustomer.setValue(cboCustomer.getItems().get(0));
            dpCheckinDate.setValue(LocalDate.now());
            dpCheckoutDate.setValue(LocalDate.now().plusDays(1));
            cboStatus.setValue("Pending");
            clearDateWarning();

            loadRooms(LocalDate.now(), LocalDate.now().plusDays(1));
            calculateTotalPrice();

            btnDelete.setDisable(true);
            btnSave.setText("Create Booking");
            tblBooking.getSelectionModel().clearSelection();
        } finally {
            isLoading = false;
        }
    }

    // ─── Filter Handlers ─────────────────────────────────────────────────────────

    @FXML
    private void handleShowAll(ActionEvent event) {
        loadBookings();
    }

    @FXML
    private void handleShowConfirmed(ActionEvent event) {
        filterByStatus("Confirmed");
    }

    @FXML
    private void handleShowPending(ActionEvent event) {
        filterByStatus("Pending");
    }

    @FXML
    private void handleShowCancelled(ActionEvent event) {
        filterByStatus("Cancelled");
    }

    private void filterByStatus(String status) {
        tblBooking.setItems(FXCollections.observableArrayList(BookingDAO.getBookingsByStatus(status)));
    }

    // ─── Search Handlers ─────────────────────────────────────────────────────────

    /**
     * Search bookings by customer name and/or date range.
     */
    @FXML
    private void handleSearch(ActionEvent event) {
        String customerName = txtSearchCustomer.getText();
        LocalDate checkinDate = dpSearchCheckin.getValue();
        LocalDate checkoutDate = dpSearchCheckout.getValue();

        // If all fields are empty, show all bookings
        if ((customerName == null || customerName.trim().isEmpty())
                && checkinDate == null && checkoutDate == null) {
            loadBookings();
            showAlert(Alert.AlertType.INFORMATION, "Search", "Showing all bookings.\nEnter search criteria to filter.");
            return;
        }

        List<Booking> results = BookingDAO.searchBookings(customerName, checkinDate, checkoutDate);
        bookingList = FXCollections.observableArrayList(results);
        tblBooking.setItems(bookingList);

        if (results.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Search Results", "No bookings found matching your criteria.");
        }
    }

    /**
     * Clear search filters and show all bookings.
     */
    @FXML
    private void handleClearSearch(ActionEvent event) {
        txtSearchCustomer.clear();
        dpSearchCheckin.setValue(null);
        dpSearchCheckout.setValue(null);
        loadBookings();
    }

    // ─── Export CSV ──────────────────────────────────────────────────────────────

    @FXML
    private void handleExportCSV(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save CSV File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        java.io.File file = fc.showSaveDialog(null);
        if (file == null)
            return;

        try (FileWriter writer = new FileWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
            writer.write('\ufeff'); // BOM for Excel
            writer.write("Booking ID,Customer,Room,Check-in,Check-out,Total Price,Status\n");
            for (Booking b : BookingDAO.getAllBookings()) {
                writer.write(String.format("%d,\"%s\",%s,%s,%s,%.0f,%s\n",
                        b.getBookingId(), b.getCustomerName(), b.getRoomNumber(),
                        b.getCheckinDate(), b.getCheckoutDate(), b.getTotalPrice(), b.getStatus()));
            }
            showAlert(Alert.AlertType.INFORMATION, "Export Successful", "CSV saved to: " + file.getName());
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Error", "Failed to export CSV: " + e.getMessage());
        }
    }

    // ─── Back ────────────────────────────────────────────────────────────────────

    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        App.setRoot("dashboard");
    }

    // ─── Utilities ───────────────────────────────────────────────────────────────

    private String formatCurrency(double amount) {
        return String.format("%,.0f VND", amount);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}