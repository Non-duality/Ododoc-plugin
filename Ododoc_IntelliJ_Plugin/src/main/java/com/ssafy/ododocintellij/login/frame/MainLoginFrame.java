package com.ssafy.ododocintellij.login.frame;

import com.ssafy.ododocintellij.login.alert.AlertHelper;
import com.ssafy.ododocintellij.login.manager.TokenManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class MainLoginFrame extends Stage {

    private static boolean isFrameVisible = false;

    public MainLoginFrame() {
        isFrameVisible = true;
        setTitle(" Ododoc");
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        Image windowIcon = new Image(getClass().getResourceAsStream("/image/button/icon.png"));
        getIcons().add(windowIcon);

        TokenManager tokenManager = TokenManager.getInstance();
        setOnCloseRequest(event -> {
            if(tokenManager.getAccessToken() == null || tokenManager.getRefreshToken() == null){
                Alert alert = AlertHelper.makeAlert(
                        Alert.AlertType.CONFIRMATION,
                        " Ododoc",
                        "로그인 필요",
                        "Ododoc 서비스를 이용하려면 로그인이 필요합니다.\n정말 종료하시겠습니까?",
                        "/image/button/icon.png"
                );

                Optional<ButtonType> result = alert.showAndWait();
                if(result.isPresent() && result.get() != ButtonType.OK) {
                    event.consume();
                }
                else{
                    isFrameVisible = false;
                }
            }
        });

        Button homeBtn = makeHomeButton();
        Button kakaoLoginBtn = makeButton("kakao"); // 카카오 로그인 버튼
        Button naverLoginBtn = makeButton("naver"); // 네이버 로그인 버튼
        Button googleLoginBtn = makeButton("google"); // 구글 로그인 버튼

        layout.getChildren().addAll(homeBtn, kakaoLoginBtn, naverLoginBtn, googleLoginBtn);
        layout.setPadding(new Insets(-15, 0, 0, 0));
        Scene scene = new Scene(layout, 300, 400);
        setScene(scene);
        show();
    }

    private void onLogin(String provider) {
        new OauthLoginFrame(this, provider);
    }

    private Button makeButton(String provider){
        String iconImagePath = "/image/button/" + provider +"_login.png";
        ImageView btnImageView = new ImageView(new Image(getClass().getResourceAsStream(iconImagePath)));
        btnImageView.setFitWidth(183);
        btnImageView.setFitHeight(45);
        Button loginBtn = new Button("", btnImageView);
        loginBtn.setStyle("-fx-background-color: transparent; -fx-padding: 3, 3, 3, 3;");

        String upperProvider = provider.toUpperCase();
        loginBtn.setOnAction(e -> {
            onLogin(upperProvider);
        });

        return loginBtn;
    }

    private Button makeHomeButton(){
        ImageView btnImageView = new ImageView(new Image(getClass().getResourceAsStream("/image/button/logo.png")));
        btnImageView.setFitWidth(184);
        btnImageView.setFitHeight(60);
        Button homeBtn = new Button("", btnImageView);
        homeBtn.setStyle("-fx-background-color: transparent; -fx-padding: 30, 0, 3, 3; -fx-border-color: transparent;");

        homeBtn.setOnAction(e -> {
            openWebPage();
        });

        return homeBtn;
    }

    private void openWebPage() {
        Platform.runLater(() -> {
            try{
                if(Desktop.isDesktopSupported()){
                    Desktop desktop = Desktop.getDesktop();
                    desktop.browse(new URI("https://k10d209.p.ssafy.io/"));
                }
            } catch (IOException | URISyntaxException e){
                e.printStackTrace();
            }
        });
    }

    public static boolean isFrameVisible() {
        return isFrameVisible;
    }


}