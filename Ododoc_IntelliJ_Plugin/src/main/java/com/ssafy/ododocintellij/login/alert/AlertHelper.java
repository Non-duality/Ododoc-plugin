package com.ssafy.ododocintellij.login.alert;

import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AlertHelper {

    public static Alert makeAlert(Alert.AlertType alertType, String title, String header, String content, String imagePath) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(AlertHelper.class.getResourceAsStream(imagePath)));

        return alert;
    }
}
