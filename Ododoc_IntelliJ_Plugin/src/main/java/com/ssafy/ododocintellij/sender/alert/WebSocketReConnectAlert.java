package com.ssafy.ododocintellij.sender.alert;

import com.ssafy.ododocintellij.login.alert.AlertHelper;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

public class WebSocketReConnectAlert {

    public static Alert makeAlert() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(" Ododoc");
        alert.setHeaderText("서버 연결 실패");
        alert.setContentText("Ododoc 서비스를 이용하려면 서버와의 연결이 필요합니다.\n다시 시도하시겠습니까?");
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.getIcons().add(new Image(AlertHelper.class.getResourceAsStream("/image/button/icon.png")));
        ImageView warningIcon = new ImageView(new Image(WebSocketReConnectAlert.class.getResourceAsStream("/image/button/warning.png")));
        alert.setGraphic(warningIcon);

        return alert;
    }
}
