package com.ssafy.ododocintellij.sender;

import com.ssafy.ododocintellij.login.alert.AlertHelper;
import com.ssafy.ododocintellij.sender.alert.WebSocketReConnectAlert;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

public class BuildResultSender {

    // volatile 키워드를 사용하여 메모리 가시성 문제 해결, 멀티스레드 환경에서도 변수가 안전하게 사용할 수 있도록 함.
    private static volatile WebSocketClient INSTANCE;

    // 객체의 락을 위해 사용.
    private static final Object lock = new Object();
    private static final String WEBSOCKET_URI = "wss://k10d209.p.ssafy.io/process/ws";
    private static boolean enableWhenPushBtn = false;
    private static int count = 0;
    private BuildResultSender() {}

    public static WebSocketClient getINSTANCE() {

        if(INSTANCE == null) {
            // 두 번째로 null 체크를 한 후 다중 스레드 환경에서 동시에 여러 인스턴스가 생성되는 것을 방지
            synchronized (lock) {
                if(INSTANCE == null){
                    try {

                        INSTANCE = new WebSocketClient(new URI(WEBSOCKET_URI)) {
                            @Override
                            public void onOpen(ServerHandshake serverHandshake) {
                                if(count > 0){
                                    Platform.runLater(() -> {
                                        Alert alert = AlertHelper.makeAlert(
                                                Alert.AlertType.INFORMATION,
                                                " Ododoc",
                                                "서버 연결 성공",
                                                "서버와의 연결에 성공했습니다.",
                                                "/image/button/icon.png"
                                        );

                                        alert.showAndWait();
                                    });
                                }
                                count ++;
                            }

                            @Override
                            public void onMessage(String s) {}

                            @Override
                            public void onClose(int i, String s, boolean b) {
                                if(count > 0){
                                    BuildResultSender.setINSTANCE(null);
                                    Platform.runLater(() -> {
                                        Alert alert = WebSocketReConnectAlert.makeAlert();
                                        Optional<ButtonType> result = alert.showAndWait();
                                        if(result.isPresent() && result.get() == ButtonType.OK) {
                                            getINSTANCE();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onError(Exception e) {
                                System.out.println(e.getMessage());
                                if(enableWhenPushBtn){
                                    Platform.runLater(() -> {
                                        Alert alert = AlertHelper.makeAlert(
                                                Alert.AlertType.ERROR,
                                                " Ododoc",
                                                "서버 연결 실패",
                                                "서버와의 연결에 실패했습니다.",
                                                "/image/button/icon.png"
                                        );

                                        alert.showAndWait();
                                    });
                                    enableWhenPushBtn = false;
                                }
                            }
                        };

                        INSTANCE.connect();
                    }catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return INSTANCE;
    }

    public static void setINSTANCE(WebSocketClient INSTANCE) {
        BuildResultSender.INSTANCE = INSTANCE;
    }

    public static void setEnableWhenPushBtn(boolean enableWhenPushBtn) {
        BuildResultSender.enableWhenPushBtn = enableWhenPushBtn;
    }

    public static void sendMessage(String message) {
        if(INSTANCE != null && INSTANCE.isOpen()){
            INSTANCE.send(message);
        }
    }

    public static boolean isConnected() {
        return INSTANCE != null && INSTANCE.isOpen() ? true : false;
    }
}
