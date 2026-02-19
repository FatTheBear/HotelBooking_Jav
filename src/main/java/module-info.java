module com.hotel.hotelbooking {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    
    opens com.hotel.hotelbooking to javafx.fxml;
    
    exports com.hotel.hotelbooking;
}