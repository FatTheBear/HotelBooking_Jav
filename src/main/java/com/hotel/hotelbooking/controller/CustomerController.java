package com.hotel.hotelbooking.controller;
import com.hotel.hotelbooking.database.CustomerDAO;
import com.hotel.hotelbooking.model.Customer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class CustomerController {

    @FXML private TextField txtName;
    @FXML private TextField txtPhone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtIdCard;

    @FXML private TableView<Customer> tableView;
    @FXML private TableColumn<Customer, Integer> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colIdCard;

    private ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        colId.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colIdCard.setCellValueFactory(new PropertyValueFactory<>("idCard"));

        tableView.setItems(customerList);

        // Khi chọn dòng -> fill lên form
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        txtName.setText(newSelection.getFullName());
                        txtPhone.setText(newSelection.getPhone());
                        txtEmail.setText(newSelection.getEmail());
                        txtIdCard.setText(newSelection.getIdCard());
                    }
                });

        // Chỉ cho nhập số vào Phone
        txtPhone.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtPhone.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        // Chỉ cho nhập số vào ID Card
        txtIdCard.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtIdCard.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });

        loadCustomers();
    }

    // ================= ADD =================
    @FXML
    private void handleAdd() {

        if (!validateInput()) return;

        try {
            Customer customer = new Customer();
            customer.setFullName(txtName.getText().trim());
            customer.setPhone(txtPhone.getText().trim());
            customer.setEmail(txtEmail.getText().trim());
            customer.setIdCard(txtIdCard.getText().trim());
            customer.setAddress("");

            CustomerDAO.insert(customer);

            loadCustomers();
            handleClear();
            showSuccess("Added successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error while adding customer!");
        }
    }

    // ================= UPDATE =================
    @FXML
    private void handleUpdate() {

        Customer selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a customer to update!");
            return;
        }

        if (!validateInput()) return;

        try {
            selected.setFullName(txtName.getText().trim());
            selected.setPhone(txtPhone.getText().trim());
            selected.setEmail(txtEmail.getText().trim());
            selected.setIdCard(txtIdCard.getText().trim());
            selected.setAddress("");

            CustomerDAO.update(selected);

            loadCustomers();
            handleClear();
            showSuccess("Updated successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error while updating customer!");
        }
    }

    // ================= DELETE =================
    @FXML
    private void handleDelete() {

        Customer selected = tableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Please select a customer to delete!");
            return;
        }

        try {
            CustomerDAO.delete(selected.getCustomerId());

            loadCustomers();
            handleClear();
            showSuccess("Deleted successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error while deleting customer!");
        }
    }

    // ================= CLEAR =================
    @FXML
    private void handleClear() {
        txtName.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtIdCard.clear();
        tableView.getSelectionModel().clearSelection();
    }

    // ================= VALIDATE =================
    private boolean validateInput() {

        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        String idCard = txtIdCard.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty() || idCard.isEmpty()) {
            showError("Please fill in all fields!");
            return false;
        }

        if (!phone.matches("\\d{10,11}")) {
            showError("Phone must be 10-11 digits!");
            return false;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showError("Invalid email format!");
            return false;
        }

        if (!idCard.matches("\\d{9,12}")) {
            showError("ID Card must be 9-12 digits!");
            return false;
        }

        return true;
    }

    // ================= ALERT =================
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================= LOAD DATA =================
    private void loadCustomers() {
        try {
            customerList.clear();
            customerList.addAll(CustomerDAO.getAllCustomers());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error loading customer data!");
        }
    }
}
