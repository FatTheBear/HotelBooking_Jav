package com.hotel.hotelbooking.controller;

import com.hotel.hotelbooking.App;
import com.hotel.hotelbooking.database.BookingDAO;
import com.hotel.hotelbooking.database.CustomerDAO;
import com.hotel.hotelbooking.database.RoomDAO;
import com.hotel.hotelbooking.model.Booking;
import com.hotel.hotelbooking.model.Customer;
import com.hotel.hotelbooking.model.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BookingController {
    
    @FXML private ComboBox<Customer> cboCustomer;
    @FXML private ComboBox<Room> cboRoom;
    @FXML private DatePicker dpCheckinDate;
    @FXML private DatePicker dpCheckoutDate;
    @FXML private ComboBox<String> cboStatus;
    @FXML private Label lblRoomPrice;
    @FXML private Label lblTotalPrice;
    
    @FXML private Button btnCreate;
    @FXML private Button btnReset;
    @FXML private Button btnShowAll;
    @FXML private Button btnShowConfirmed;
    @FXML private Button btnShowPending;
    @FXML private Button btnShowCancelled;
    @FXML private Button btnExportCSV;
    @FXML private Button btnRefresh;
    @FXML private Button btnBack;
    
    @FXML private TableView<Booking> tblBooking;
    @FXML private TableColumn<Booking, Integer> colBookingId;
    @FXML private TableColumn<Booking, String> colCustomer;
    @FXML private TableColumn<Booking, String> colRoom;
    @FXML private TableColumn<Booking, LocalDate> colCheckin;
    @FXML private TableColumn<Booking, LocalDate> colCheckout;
    @FXML private TableColumn<Booking, Double> colTotalPrice;
    @FXML private TableColumn<Booking, String> colStatus;
    @FXML private TableColumn<Booking, String> colAction;
    
    private ObservableList<Booking> bookingList;
    
    @FXML
    public void initialize() {
        // Setup ComboBox cho Status
        cboStatus.setItems(FXCollections.observableArrayList(
            "Pending", "Confirmed", "Cancelled"
        ));
        cboStatus.setValue("Pending");
        
        // Initialize DatePickers with today's date
        dpCheckinDate.setValue(LocalDate.now());
        dpCheckoutDate.setValue(LocalDate.now().plusDays(1));
        
        // Load dữ liệu từ database
        loadCustomers();
        loadRooms();
        loadBookings();
        
        // Setup Table Columns
        setupTableColumns();
        
        // Add listeners
        cboRoom.setOnAction(e -> calculateTotalPrice());
        dpCheckinDate.setOnAction(e -> calculateTotalPrice());
        dpCheckoutDate.setOnAction(e -> calculateTotalPrice());
        
        // Calculate initial price
        calculateTotalPrice();
    }
    
    /**
     * Load danh sách khách hàng từ database
     */
    private void loadCustomers() {
        List<Customer> customers = CustomerDAO.getAllCustomers();
        cboCustomer.setItems(FXCollections.observableArrayList(customers));
        if (!customers.isEmpty()) {
            cboCustomer.setValue(customers.get(0));
        }
    }
    
    /**
     * Load danh sách phòng từ database
     */
    private void loadRooms() {
        List<Room> rooms = RoomDAO.getAvailableRooms();
        cboRoom.setItems(FXCollections.observableArrayList(rooms));
        if (!rooms.isEmpty()) {
            cboRoom.setValue(rooms.get(0));
        }
    }
    
    /**
     * Load danh sách booking từ database
     */
    private void loadBookings() {
        try {
            List<Booking> bookings = BookingDAO.getAllBookings();
            bookingList = FXCollections.observableArrayList(bookings);
            tblBooking.setItems(bookingList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Setup các cột của Table
     */
    private void setupTableColumns() {
        // Use lambda expressions instead of PropertyValueFactory to bypass module access issues
        colBookingId.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getBookingId()).asObject());
        colCustomer.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCustomerName()));
        colRoom.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRoomNumber()));
        colCheckin.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCheckinDate()));
        colCheckout.setCellValueFactory(cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCheckoutDate()));
        colTotalPrice.setCellValueFactory(cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getTotalPrice()).asObject());
        colStatus.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatus()));
        
        // Action column (Delete only)
        colAction.setCellFactory(param -> new TableCell<Booking, String>() {
            private final Button btnDelete = new Button("🗑️ Xóa");
            
            {
                btnDelete.setStyle("-fx-padding: 5; -fx-font-size: 11;");
                
                btnDelete.setOnAction(e -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    handleDeleteBooking(booking);
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hBox = new HBox(5);
                    hBox.getChildren().add(btnDelete);
                    setGraphic(hBox);
                }
            }
        });
    }
    
    /**
     * Tính tổng tiền dựa trên số đêm và giá phòng
     */
    private void calculateTotalPrice() {
        try {
            Room room = cboRoom.getValue();
            LocalDate checkIn = dpCheckinDate.getValue();
            LocalDate checkOut = dpCheckoutDate.getValue();
            
            if (room != null && checkIn != null && checkOut != null) {
                if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                    lblTotalPrice.setText("❌ Ngày không hợp lệ");
                    return;
                }
                
                long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
                double totalPrice = room.getPrice() * nights;
                
                lblRoomPrice.setText(String.format("%.0f VND", room.getPrice()));
                lblTotalPrice.setText(String.format("%.0f VND", totalPrice));
            }
        } catch (Exception e) {
            lblTotalPrice.setText("0 VND");
        }
    }
    
    /**
     * Lấy tổng tiền từ label (đã tính sẵn)
     */
    private double getTotalPriceFromLabel() {
        String text = lblTotalPrice.getText().replace(" VND", "").trim();
        try {
            return Double.parseDouble(text);
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Xử lý tạo booking mới
     */
    @FXML
    private void handleCreateBooking(ActionEvent event) {
        try {
            Customer customer = cboCustomer.getValue();
            Room room = cboRoom.getValue();
            LocalDate checkIn = dpCheckinDate.getValue();
            LocalDate checkOut = dpCheckoutDate.getValue();
            String status = cboStatus.getValue();
            
            // Validation
            if (customer == null || room == null || checkIn == null || checkOut == null) {
                showAlert(Alert.AlertType.WARNING, "⚠️ Lỗi", "Vui lòng điền đầy đủ thông tin!");
                return;
            }
            
            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                showAlert(Alert.AlertType.WARNING, "⚠️ Lỗi", "Ngày checkout phải sau checkin!");
                return;
            }
            
            // Lấy tổng tiền từ label (đã tính sẵn)
            double totalPrice = getTotalPriceFromLabel();
            
            // Tạo booking mới
            Booking booking = new Booking(
                0,
                customer.getCustomerId(),
                room.getRoomId(),
                checkIn,
                checkOut,
                totalPrice,
                status
            );
            
            if (BookingDAO.createBooking(booking)) {
                showAlert(Alert.AlertType.INFORMATION, "✅ Thành công", "Đặt phòng thành công!");
                handleReset(null);
                loadBookings();
            } else {
                showAlert(Alert.AlertType.ERROR, "❌ Lỗi", "Không thể tạo booking!");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "❌ Lỗi", "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Reset form
     */
    @FXML
    private void handleReset(ActionEvent event) {
        if (!cboCustomer.getItems().isEmpty()) {
            cboCustomer.setValue(cboCustomer.getItems().get(0));
        }
        if (!cboRoom.getItems().isEmpty()) {
            cboRoom.setValue(cboRoom.getItems().get(0));
        }
        dpCheckinDate.setValue(LocalDate.now());
        dpCheckoutDate.setValue(LocalDate.now().plusDays(1));
        cboStatus.setValue("Pending");
        lblRoomPrice.setText("0 VND");
        lblTotalPrice.setText("0 VND");
    }
    
    /**
     * Hiển thị tất cả booking
     */
    @FXML
    private void handleShowAll(ActionEvent event) {
        loadBookings();
    }
    
    /**
     * Hiển thị booking đã xác nhận
     */
    @FXML
    private void handleShowConfirmed(ActionEvent event) {
        showBookingsByStatus("Confirmed");
    }
    
    /**
     * Hiển thị booking chờ xác nhận
     */
    @FXML
    private void handleShowPending(ActionEvent event) {
        showBookingsByStatus("Pending");
    }
    
    /**
     * Hiển thị booking đã hủy
     */
    @FXML
    private void handleShowCancelled(ActionEvent event) {
        showBookingsByStatus("Cancelled");
    }
    
    /**
     * Helper method - Hiển thị booking theo status
     */
    private void showBookingsByStatus(String status) {
        List<Booking> bookings = BookingDAO.getBookingsByStatus(status);
        tblBooking.setItems(FXCollections.observableArrayList(bookings));
    }
    
    /**
     * Xóa booking
     */
    private void handleDeleteBooking(Booking booking) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText("Xóa Booking");
        confirm.setContentText("Bạn có chắc muốn xóa booking này?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (BookingDAO.deleteBooking(booking.getBookingId())) {
                showAlert(Alert.AlertType.INFORMATION, "✅ Thành công", "Xóa booking thành công!");
                loadBookings();
            } else {
                showAlert(Alert.AlertType.ERROR, "❌ Lỗi", "Không thể xóa booking!");
            }
        }
    }
    
    /**
     * Xuất danh sách booking ra CSV
     */
    @FXML
    private void handleExportCSV(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Lưu file CSV");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            
            java.io.File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                exportBookingsToCSV(file);
                showAlert(Alert.AlertType.INFORMATION, "✅ Thành công", 
                    "Xuất CSV thành công!\nFile: " + file.getName());
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "❌ Lỗi", "Lỗi xuất CSV: " + e.getMessage());
        }
    }
    
    /**
     * Export bookings to CSV file
     */
    private void exportBookingsToCSV(java.io.File file) {
        try (FileWriter writer = new FileWriter(file)) {
            // Write header
            writer.write("Booking ID,Khách Hàng,Phòng,Check-in,Check-out,Tổng Tiền,Trạng Thái\n");
            
            // Write data
            List<Booking> bookings = BookingDAO.getAllBookings();
            for (Booking booking : bookings) {
                writer.write(String.format("%d,\"%s\",%s,%s,%s,%.0f,%s\n",
                    booking.getBookingId(),
                    booking.getCustomerName(),
                    booking.getRoomNumber(),
                    booking.getCheckinDate(),
                    booking.getCheckoutDate(),
                    booking.getTotalPrice(),
                    booking.getStatus()
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Làm mới danh sách
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadBookings();
        showAlert(Alert.AlertType.INFORMATION, "✅ Thành công", "Làm mới danh sách thành công!");
    }
    
    /**
     * Quay lại dashboard
     */
    @FXML
    private void handleBack(ActionEvent event) throws IOException {
        App.setRoot("dashboard");
    }
    
    /**
     * Show alert helper
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
