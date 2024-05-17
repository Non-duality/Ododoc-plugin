package com.ssafy.ododocintellij.login.frame;

import com.intellij.execution.ExecutionManager;
import com.intellij.openapi.project.Project;
import com.ssafy.ododocintellij.directory.frame.DirectoryFrame;
import com.ssafy.ododocintellij.directory.manager.DirectoryInfoManager;
import com.ssafy.ododocintellij.login.alert.AlertHelper;
import com.ssafy.ododocintellij.login.manager.TokenManager;
import com.ssafy.ododocintellij.sender.BuildResultSender;
import com.ssafy.ododocintellij.sender.alert.WebSocketReConnectAlert;
import com.ssafy.ododocintellij.tracker.CodeListener;
import com.ssafy.ododocintellij.tracker.manager.ProjectProvider;
import com.ssafy.ododocintellij.tracker.manager.ProjectTracker;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.net.CookiePolicy.ACCEPT_ALL;

public class OauthLoginFrame extends Stage {

    private String clientId;
    private String redirectUri;
    private String firstLocation;
    private String lastLocation;
    private String loginUri;
    private final int TIME_OUT = 5; // 로그인 응답 대기 시간

    private ScheduledExecutorService scheduler;
    private MainLoginFrame mainLoginFrame = null;
    private ProgressIndicator loadingIndicator;
    private Alert alert;

    public OauthLoginFrame(MainLoginFrame mainLoginFrame, String provider){
        this.mainLoginFrame = mainLoginFrame;

        // 제목 설정
        setTitle(" " + provider);

        String lowerProvider = provider.toLowerCase();
        redirectUri = "https://k10d209.p.ssafy.io/api/oauth2/authorization/" + lowerProvider;

        // 아이콘 설정
        String iconImagePath = "/image/button/" + lowerProvider  + "_icon.png";
        Image windowIcon = new Image(getClass().getResourceAsStream(iconImagePath));
        getIcons().add(windowIcon);

        StackPane layout = new StackPane();
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);

        layout.getChildren().addAll(webView, loadingIndicator);
        Scene scene = new Scene(layout, 450, 600);
        setScene(scene);
        show();

        // alert 초기화
        alert = AlertHelper.makeAlert(
                Alert.AlertType.WARNING,
                " " + provider,
                "로그인 실패",
                "다시 로그인 해주세요.",
                iconImagePath);

        // oauth 플랫폼에 따라 필드 초기화
        switch(provider) {
            case "KAKAO" :
                clientId = "a23282fc18f2b445d559dfe93fa96e6b";
                firstLocation = "kakaossotokenlogin.do";
                lastLocation = redirectUri;
                loginUri = "https://kauth.kakao.com/oauth/authorize?response_type=code&client_id="
                        + clientId
                        + "&redirect_uri="
                        + redirectUri;
                break;
            case "NAVER" :
                clientId = "DRnVNgGzq_x_6Q4apfhJ";
                firstLocation = "oauth_token";
                lastLocation = "nid";
                loginUri = "https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id="
                        + clientId
                        + "&redirect_uri="
                        + redirectUri;
                break;
            case "GOOGLE" :
                clientId = "599323777848-68aq3cu9p98np6eml1m77mfc1ethpkrp.apps.googleusercontent.com";
                firstLocation = "SetSID";
                lastLocation = "Fuserinfo.profile";
                loginUri = "https://accounts.google.com/o/oauth2/v2/auth?client_id="
                        + clientId
                        + "&redirect_uri="
                        + redirectUri
                        + "&scope=profile&response_type=code";
                break;
        }

        doOauthLogin(webEngine, provider);
    }

    private void doOauthLogin(WebEngine webEngine, String provider) {

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        TokenManager tokenManager = TokenManager.getInstance();
        DirectoryInfoManager directoryInfoManager = DirectoryInfoManager.getInstance();

        if(scheduler != null && !scheduler.isShutdown()){
            scheduler.shutdownNow();
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
        Runnable timeoutTask = () -> {
            Platform.runLater(() -> {
                alert.showAndWait();
                close();
                cookieManager.getCookieStore().removeAll();
            });
        };

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {

            // 화면이 성공적으로 전환이 되었을 때
            if (newState == Worker.State.SUCCEEDED) {

                // 로그인 응답 시간 스케쥴러 등록, 로딩 스피너 작동
                if (webEngine.getLocation().contains(firstLocation)){
                    scheduler.schedule(timeoutTask, TIME_OUT, TimeUnit.SECONDS);
                    loadingIndicator.setVisible(true);
                }

                boolean shouldProcess = false;
                switch (provider){
                    case "KAKAO" :
                        shouldProcess = webEngine.getLocation().contains(redirectUri);
                        break;
                    case "NAVER" :
                        shouldProcess = webEngine.getLocation().contains(redirectUri) && !webEngine.getLocation().contains(lastLocation);
                        break;
                    case "GOOGLE" :
                        shouldProcess = webEngine.getLocation().contains(redirectUri) && webEngine.getLocation().contains(lastLocation);
                        break;
                }

                // 응답을 받을 화면이 나온다면
                if (shouldProcess) {

                    scheduler.shutdownNow();
                    loadingIndicator.setVisible(false);

                    // javascript를 실행시켜 content 정보 가져오기
                    String content = (String) webEngine.executeScript("document.body.textContent");
                    Long status;
                    JSONParser jsonParser = new JSONParser();

                    try {
                        // String to Json
                        JSONObject json = (JSONObject) jsonParser.parse(content);
                        status = (Long) json.get("status");
                        JSONObject data = (JSONObject) json.get("data");

                        if (status != 200) {
                            alert.showAndWait();
                            close();
                            cookieManager.getCookieStore().removeAll();
                        } else {
                            // access 토큰을 싱글톤 객체에 저장
                            tokenManager.setAccessToken((String) data.get("accessToken"));
                            directoryInfoManager.setRootId((long) data.get("rootId"));
                            directoryInfoManager.setTitle((String) data.get("title"));
                        }


                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    webEngine.executeScript("document.body.style.display = 'none';");

                    // 쿠키의 refresh 토큰을 싱글톤 객체에 저장
                    cookieManager.getCookieStore().getCookies().forEach(cookie -> {
                        if (cookie.getName().equals("refreshToken")) {
                            tokenManager.setRefreshToken(cookie.getValue());
                        }
                    });

                    // 로그인 성공 여부 다시 확인
                    if(tokenManager.getAccessToken() == null || tokenManager.getRefreshToken() == null){
                        Platform.runLater(() -> {
                            alert.showAndWait();
                            close();
                            cookieManager.getCookieStore().removeAll();
                        });

                        return;
                    }

                    // 지금 현재 등록되어 있는 모든 프로젝트들에게 codeListener 추가하기
                    addCodeListener(ProjectProvider.getInstance());

                    try {
                        new DirectoryFrame().start(mainLoginFrame);
                        connectWebSocket();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                    close();
                }
            }

        });

        webEngine.load(loginUri);
    }

    // Queue에 있는 project 객체에 codeListener 추가해주기.
    private void addCodeListener(ProjectProvider projectProvider){
        int size = projectProvider.getProjects().size();
        ProjectTracker projectTracker = ProjectTracker.getInstance();
        Project tempProject = null;
        for(int i = 0; i < size; i++){
            tempProject = projectProvider.getProjects().poll();
            if(tempProject.isOpen()){
                tempProject.getMessageBus().connect().subscribe(ExecutionManager.EXECUTION_TOPIC, new CodeListener(tempProject));
                projectTracker.initHashStatus(tempProject);
            }
        }
    }

    // 처리 서버와 webSocket 연결해주기
    private void connectWebSocket() {
        BuildResultSender.setINSTANCE(null);
        BuildResultSender.getINSTANCE();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if(!BuildResultSender.isConnected()){
            Platform.runLater(() -> {
                Alert alert = WebSocketReConnectAlert.makeAlert();
                Optional<ButtonType> result = alert.showAndWait();
                if(result.isPresent() && result.get() == ButtonType.OK) {
                    connectWebSocket();
                }
                else {
                    this.close();
                }
            });
        }
    }

}
