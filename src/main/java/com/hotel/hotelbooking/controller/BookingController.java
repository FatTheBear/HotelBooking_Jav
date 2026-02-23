package com.hotel.hotelbooking.controller;

import com.hotel.hotelbooking.App;
import com.hotel.hotelbooking.database.BookingDAO;
import com.hotel.hotelbooking.database.CustomerDAO;
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

public class BookingController {

    // ─── Form Fields ─────────────────────────────────────────────────────────────
    @FXML private ComboBox<Customer> cboCustomer;
    @FXML private ComboBox<Room>     cboRoom;
    @FXML private DatePicker         dpCheckinDate;
    @FXML private DatePicker         dpCheckoutDate;
    @FXML private ComboBox<String>   cboStatus;
    @FXML private Label              lblRoomPrice;
    @FXML private Label              lblTotalPrice;
    @FXML private Label              lblDateWarning;   // inline warning label

    // ─── Form Buttons ─────────────────────────────────────────────────────────────
    @FXML private Button btnSave;       // Create or Update
    @FXML private Button btnDelete;     // Delete selected booking
    @FXML private Button btnReset;      // Clear / new form
    @FXML private Button btnExportCSV;
    @FXML private Button btnBack;

    // ─── Filter Buttons ───────────────────────────────────────────────────────────
    @FXML private Button btnShowAll;
    @FXML private Button btnShowConfirmed;
    @FXML private Button btnShowPending;
    @FXML private Button btnShowCancelled;

    // ─── Table ────────────────────────────────────────────────────────────────────
    @FXML private TableView<Booking>              tblBooking;
    @FXML private TableColumn<Booking, Integer>   colBookingId;
    @FXML private TableColumn<Booking, String>    colCustomer;
    @FXML private TableColumn<Booking, String>    colRoom;
    @FXML private TableColumn<Booking, LocalDate> colCheckin;
    @FXML private TableColumn<Booking, LocalDate> colCheckout;
    @FXML private TableColumn<Booking, Double>    colTotalPrice;
    @FXML private TableColumn<Booking, String>    colStatus;

    // ─── State ────────────────────────────────────────────────────────────────────
    private ObservableList<Booking> bookingList;
    /** The booking currently loaded into the form (null = new booking mode). */
    private Booking selectedBooking = null;
    /** Whether the form has unsaved changes relative to the last loaded/saved state. */
    private boolean isDirty = false;
    /** Suppresses dirty-marking while the form is being programmatically populated. */
    private boolean isLoading = false;
    /** Suppresses the selection listener during programmatic selection revert. */
    private boolean isRevertingSelection = false;

    // ─── Initialization ──────────────────────────────────────────────────────────

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

    // ─── Data Loading ─────────────────────────────────────────────────────────────

    private void loadCustomers() {
        List<Customer> customers = CustomerDAO.getAllCustomers();
        cboCustomer.setItems(FXCollections.observableArrayList(customers));
        if (!customers.isEmpty()) {
            cboCustomer.setValue(customers.get(0));
        }
    }

    /**
     * Load rooms available for the given date range.
     * - In UPDATE mode: uses the overloaded method that excludes the current booking,
     *   so the room already assigned to this booking remains selectable.
     * - In CREATE mode: uses the standard method.
     * - Falls back to all available rooms if dates are invalid.
     */
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
        colBookingId.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getBookingId()).asObject());
        colCustomer.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleStringProperty(cd.getValue().getCustomerName()));
        colRoom.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleStringProperty(cd.getValue().getRoomNumber()));
        colCheckin.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getCheckinDate()));
        colCheckout.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getCheckoutDate()));
        colTotalPrice.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleDoubleProperty(cd.getValue().getTotalPrice()).asObject());
        colStatus.setCellValueFactory(cd ->
            new javafx.beans.property.SimpleStringProperty(cd.getValue().getStatus()));

        // Format currency
        colTotalPrice.setCellFactory(col -> new TableCell<Booking, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : formatCurrency(item));
            }
        });

    }

    /**
     * When a row is selected, load its data into the form.
     * If the form is dirty, ask the user first.
     * - "Keep Editing"   → revert table highlight to the previously selected row (via Platform.runLater
     *                       so the listener has already returned before the selection change fires).
     * - "Discard & Load" → load the new row and refresh the table.
     */
    private void setupTableSelection() {
        tblBooking.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Skip if this is a programmatic revert
            if (isRevertingSelection) return;
            if (newVal == null) return;

            if (isDirty) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Unsaved Changes");
                confirm.setHeaderText("You have unsaved changes.");
                confirm.setContentText("Do you want to discard changes and load the selected booking?");
                ButtonType btnDiscard = new ButtonType("Discard & Load");
                ButtonType btnContinue = new ButtonType("Keep Editing", ButtonBar.ButtonData.CANCEL_CLOSE);
                confirm.getButtonTypes().setAll(btnDiscard, btnContinue);

                Optional<ButtonType> result = confirm.showAndWait();
                if (result.isEmpty() || result.get() != btnDiscard) {
                    // "Keep Editing": revert table highlight to the old row.
                    // Use Platform.runLater so this listener has fully returned first,
                    // then use isRevertingSelection to suppress the listener during revert.
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
                // "Discard & Load": refresh the table, then load the fresh version of the new row
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

    /**
     * Mark form as dirty whenever any input changes.
     */
    private void setupDirtyListeners() {
        cboCustomer.setOnAction(e -> { markDirty(); calculateTotalPrice(); });
        cboRoom.setOnAction(e -> { markDirty(); calculateTotalPrice(); });
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

    // ─── Form Population ─────────────────────────────────────────────────────────

    /**
     * Load a booking's data into the top form for editing.
     * Uses isLoading flag to prevent dirty-marking during population.
     */
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
                        Room bookedRoom = com.hotel.hotelbooking.database.RoomDAO.getRoomById(booking.getRoomId());
                        if (bookedRoom != null) {
                            cboRoom.getItems().add(0, bookedRoom);
                            cboRoom.setValue(bookedRoom);
                        }
                    }
                );

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

    // ─── FXML Handlers ───────────────────────────────────────────────────────────

    /**
     * Save = Create (if no booking selected) or Update (if a booking is loaded).
     */
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
                showAlert(Alert.AlertType.INFORMATION, "Success", "Booking created successfully!");
                handleReset(null);
                loadBookings();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create booking.");
            }
        } else {
            // ── UPDATE ──
            selectedBooking.setCustomerId(customer.getCustomerId());
            selectedBooking.setRoomId(room.getRoomId());
            selectedBooking.setCheckinDate(checkIn);
            selectedBooking.setCheckoutDate(checkOut);
            selectedBooking.setTotalPrice(totalPrice);
            selectedBooking.setStatus(status);

            if (BookingDAO.updateBooking(selectedBooking)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Booking updated successfully!");
                isDirty = false;
                loadBookings();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update booking.");
            }
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

    /**
     * Called when the user clicks on the empty area of the "New / Edit Booking" pane.
     * Currently disabled - clicking empty area will NOT reset the form.
     * If you want to enable reset, uncomment the code below.
     */
    @FXML
    private void handlePaneClick(MouseEvent event) {
        // Click on empty area no longer triggers reset
        // Uncomment below to re-enable reset functionality:
        // handleReset(null);
    }

    /**
     * Reset form to "new booking" state.
     * Uses isLoading to suppress dirty-marking during reset.
     */
    @FXML
    private void handleReset(ActionEvent event) {
        isLoading = true;
        try {
            selectedBooking = null;
            isDirty = false;

            if (!cboCustomer.getItems().isEmpty()) cboCustomer.setValue(cboCustomer.getItems().get(0));
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

    @FXML private void handleShowAll(ActionEvent event)       { loadBookings(); }
    @FXML private void handleShowConfirmed(ActionEvent event) { filterByStatus("Confirmed"); }
    @FXML private void handleShowPending(ActionEvent event)   { filterByStatus("Pending"); }
    @FXML private void handleShowCancelled(ActionEvent event) { filterByStatus("Cancelled"); }

    private void filterByStatus(String status) {
        tblBooking.setItems(FXCollections.observableArrayList(BookingDAO.getBookingsByStatus(status)));
    }

    // ─── Export CSV ──────────────────────────────────────────────────────────────

    @FXML
    private void handleExportCSV(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save CSV File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        java.io.File file = fc.showSaveDialog(null);
        if (file == null) return;

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
