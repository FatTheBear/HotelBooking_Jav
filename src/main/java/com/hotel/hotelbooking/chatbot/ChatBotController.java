package com.hotel.hotelbooking.chatbot;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.text.Normalizer;
import java.util.regex.Pattern;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ChatBotController {

    @FXML
    private VBox chatBox;
    
    @FXML
    private TextField inputField;
    
    @FXML
    private ScrollPane scrollPane;
    
    @FXML
    private Button backButton;

    private String savedDate = "";

    @FXML
    private void handleBack() {
        try {
            com.hotel.hotelbooking.App.setRoot("dashboard");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSend() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) {
            return;
        }
        addBubble(text, true);
        inputField.clear();
        PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
        pause.setOnFinished(e -> {
            String botResponse = generateLogic(text);
            addBubble(botResponse, false);
        });
        pause.play();
    }

    private void addBubble(String message, boolean isUser) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.getStyleClass().add(isUser ? "user-msg" : "bot-msg");
        
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888; -fx-padding: 0 5 0 5;");
        
        VBox messageWrapper = new VBox(label, timeLabel);
        messageWrapper.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        messageWrapper.setSpacing(2);
        
        HBox container = new HBox(messageWrapper);
        container.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        VBox.setMargin(container, new Insets(5, 0, 5, 0));
        
        chatBox.getChildren().add(container);
        
        PauseTransition scrollPause = new PauseTransition(Duration.millis(50));
        scrollPause.setOnFinished(event -> scrollPane.setVvalue(1.0));
        scrollPause.play();
    }

    private String removeAccent(String s) {
        if (s == null) {
            return "";
        }
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }

    private String generateLogic(String input) {
        String cleanInput = removeAccent(input.toLowerCase());

        // 1. NHẬN DIỆN SỐ ĐIỆN THOẠI
        if (input.matches(".*\\d{9,11}.*")) {
            String msg = "Tuyệt vời! 😍 Mình đã nhận được số điện thoại: " + input + ".";
            if (!savedDate.isEmpty()) {
                msg += " Lễ tân sẽ gọi xác nhận phòng cho " + savedDate + " của bạn ngay ạ!";
            } else {
                msg += " Nhân viên sẽ liên hệ tư vấn cho bạn trong ít phút nữa nhé!";
            }
            return msg + " ✅";
        }

        // 2. LOGIC ĐẶT PHÒNG & THỜI GIAN (Ưu tiên cao nhất)
        if (cleanInput.contains("dat phong") || cleanInput.contains("book") || cleanInput.contains("con phong") || cleanInput.contains("co phong")) {
            if (cleanInput.contains("mai")) {
                savedDate = "ngày mai";
                return "Dạ ngày mai bên mình vẫn còn phòng trống ạ! 😍 Bạn muốn lấy phòng Đơn, Đôi hay VIP để mình giữ chỗ luôn?";
            }
            if (cleanInput.contains("hom nay") || cleanInput.contains("toi nay")) {
                savedDate = "tối nay";
                return "Dạ tối nay tụi mình vẫn còn đủ các hạng phòng phục vụ bạn. Bạn dự định mấy giờ check-in ạ?";
            }
            return "Chào mừng bạn! 🥰 Bạn dự định ghé thăm Paradise vào ngày nào để mình kiểm tra các hạng phòng giúp bạn?";
        }

        // 3. XỬ LÝ KHI KHÁCH CHỌN LOẠI PHÒNG CỤ THỂ
        if (cleanInput.contains("vip") || cleanInput.contains("luxury")) {
            if (cleanInput.contains("gia") || cleanInput.contains("bao nhieu")) {
                return "Phòng VIP bên mình cực sang chảnh, giá là 1.500.000 VNĐ/đêm (đã gồm buffet sáng cao cấp) ạ. 💎";
            }
            return "Phòng VIP view biển hiện đang là hạng phòng hot nhất! Bạn muốn chốt đặt luôn hay cần xem hình ảnh ạ? 😍";
        }

        if (cleanInput.contains("don") || cleanInput.contains("1 nguoi")) {
            if (cleanInput.contains("gia") || cleanInput.contains("bao nhieu")) {
                return "Phòng đơn có giá 500.000 VNĐ/đêm. Lưu ý nhỏ là hạng phòng này không bao gồm buffet sáng bạn nhé! 😊";
            }
            return "Dạ phòng đơn gọn gàng, tiện nghi giá chỉ 500k. Bạn có muốn mình đặt phòng này cho bạn không?";
        }

        if (cleanInput.contains("doi") || cleanInput.contains("2 nguoi")) {
            if (cleanInput.contains("gia") || cleanInput.contains("bao nhieu")) {
                return "Phòng đôi rộng rãi giá 800.000 VNĐ/đêm, giá này đã bao gồm buffet sáng miễn phí rồi ạ! 🍳";
            }
            return "Vâng, phòng đôi rất phù hợp cho 2 người. Bạn có muốn mình giữ phòng này không?";
        }

        // 4. CÁC CÂU LỆNH XÁC NHẬN
        if (cleanInput.contains("dung roi") || cleanInput.contains("ok") || cleanInput.contains("chot") || cleanInput.contains("dong y")) {
            return "Vâng ạ! Rất vui vì bạn đã tin tưởng Paradise. 😍 Bạn vui lòng để lại SỐ ĐIỆN THOẠI để mình hoàn tất thủ tục giữ phòng nhé!";
        }

        // 5. TIỆN ÍCH & THÔNG TIN KHÁC
        if (cleanInput.contains("buffet") || cleanInput.contains("an sang")) {
            return "Dạ, buffet sáng buffet phục vụ từ 6h-9h. Miễn phí cho phòng Đôi và VIP, phòng Đơn sẽ có phí phụ thu nhẹ nếu bạn muốn dùng thêm ạ.";
        }
        if (cleanInput.contains("hinh") || cleanInput.contains("anh") || cleanInput.contains("xem phong")) {
            return "Dạ mình đã gửi hình ảnh thực tế vào tin nhắn riêng cho bạn rồi đó. Bạn kiểm tra giúp mình nhé! 📸";
        }
        if (cleanInput.contains("dia chi") || cleanInput.contains("o dau")) {
            return "Hotel Paradise tọa lạc tại 123 Đường Biển, Đà Nẵng. Rất mong được đón tiếp bạn! 🏨";
        }

        // 6. CHÀO HỎI & TẠM BIỆT
        if (cleanInput.contains("chao") || cleanInput.contains("hello") || cleanInput.contains("hi")) {
            return "Chào bạn! Chào mừng bạn đến với Hotel Paradise. Mình có thể hỗ trợ gì cho chuyến đi của bạn không ạ? ✨";
        }
        if (cleanInput.contains("tam biet") || cleanInput.contains("cam on") || cleanInput.contains("bye")) {
            return "Cảm ơn bạn đã quan tâm. Chúc bạn một ngày tốt lành và hẹn gặp lại tại Paradise! 😊";
        }

        return "Xin lỗi, mình chưa hiểu ý bạn lắm. Bạn muốn đặt phòng hay cần hỏi thêm về dịch vụ nào ạ?";
    }
}
