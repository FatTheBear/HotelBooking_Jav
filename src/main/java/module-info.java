module com.hotel.hotelbooking {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.hotel.hotelbooking to javafx.fxml;
    opens com.hotel.hotelbooking.controller to javafx.fxml;
    opens com.hotel.hotelbooking.model to javafx.base;

    exports com.hotel.hotelbooking;
}