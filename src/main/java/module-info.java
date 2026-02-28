module com.hotel.hotelbooking {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.sql;

    opens com.hotel.hotelbooking to javafx.fxml;
    opens com.hotel.hotelbooking.controller to javafx.fxml;
    opens com.hotel.hotelbooking.model to javafx.base;
    opens com.hotel.hotelbooking.chatbot to javafx.fxml;

    exports com.hotel.hotelbooking;
    exports com.hotel.hotelbooking.chatbot;
}