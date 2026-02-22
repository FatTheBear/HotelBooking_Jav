package com.hotel.hotelbooking.controller;

import com.hotel.hotelbooking.model.Customer;
import com.hotel.hotelbooking.dao.CustomerDAO;
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

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        txtName.setText(newSelection.getFullName());
                        txtPhone.setText(newSelection.getPhone());
                        txtEmail.setText(newSelection.getEmail());
                        txtIdCard.setText(newSelection.getIdCard());
                    }
                });

        loadCustomers();
    }

    @FXML
    private void handleAdd() {
        try {
            Customer customer = new Customer();
            customer.setFullName(txtName.getText());
            customer.setPhone(txtPhone.getText());
            customer.setEmail(txtEmail.getText());
            customer.setIdCard(txtIdCard.getText());
            customer.setAddress("");

            CustomerDAO.insert(customer);

            loadCustomers();
            handleClear();
            showAlert("Added successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdate() {
        Customer selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                selected.setFullName(txtName.getText());
                selected.setPhone(txtPhone.getText());
                selected.setEmail(txtEmail.getText());
                selected.setIdCard(txtIdCard.getText());
                selected.setAddress("");

                CustomerDAO.update(selected);

                loadCustomers();
                handleClear();
                showAlert("Updated successfully!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDelete() {
        Customer selected = tableView.getSelectionModel().getSelectedItem();

        if (selected != null) {
            try {
                CustomerDAO.delete(selected.getCustomerId());

                loadCustomers();
                handleClear();
                showAlert("Deleted successfully!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleClear() {
        txtName.clear();
        txtPhone.clear();
        txtEmail.clear();
        txtIdCard.clear();
        tableView.getSelectionModel().clearSelection();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadCustomers() {
        try {
            customerList.clear();
            customerList.addAll(CustomerDAO.getAll());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}