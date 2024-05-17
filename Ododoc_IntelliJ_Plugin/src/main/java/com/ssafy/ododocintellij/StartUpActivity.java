package com.ssafy.ododocintellij;

import com.intellij.execution.ExecutionManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.ssafy.ododocintellij.login.alert.AlertHelper;
import com.ssafy.ododocintellij.login.frame.MainLoginFrame;
import com.ssafy.ododocintellij.login.manager.TokenManager;
import com.ssafy.ododocintellij.tracker.CodeListener;
import com.ssafy.ododocintellij.tracker.manager.ProjectProvider;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StartUpActivity implements ProjectActivity {
    private static boolean javafxInitialized = false;


    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        TokenManager tokenManager = TokenManager.getInstance();
        ProjectProvider projectProvider = ProjectProvider.getInstance();
        projectProvider.getProjects().offer(project);
//        project.getService(DisposableService.class);
        initializeJavaFXPlatform();
        checkAndAddCodeListener(tokenManager, projectProvider);

        return null;
    }



    private synchronized void initializeJavaFXPlatform() {
        // JavaFX 플랫폼 초기화 상태 체크 및 조건부 초기화
        if(!javafxInitialized){
            Platform.startup(() -> {
                javafxInitialized = true;
                Platform.setImplicitExit(false);
                new MainLoginFrame();
            });
        }
    }

    private void checkAndAddCodeListener(TokenManager tokenManager, ProjectProvider projectProvider) {
        if(tokenManager.getAccessToken() == null || tokenManager.getRefreshToken() == null){
//            showLoginAlert();
        }
        else{
            addCodeListener(projectProvider);
        }
    }

    private void showLoginAlert() {
        Platform.runLater(() -> {
            Alert alert = AlertHelper.makeAlert(
                    Alert.AlertType.WARNING,
                    "Ododoc IntelliJ Plugin",
                    "로그인 필요",
                    "자동 정리 기능을 사용하려면 로그인이 필요합니다.",
                    "/image/button/icon.png"
            );
            alert.showAndWait();
        });
    }

    // Queue에 있는 project 객체에 codeListener 추가해주기.
    private void addCodeListener(ProjectProvider projectProvider){
        int size = projectProvider.getProjects().size();
        Project tempProject = null;
        for(int i = 0; i < size; i++){
            tempProject = projectProvider.getProjects().poll();
            if(tempProject.isOpen()){
                tempProject.getMessageBus().connect().subscribe(ExecutionManager.EXECUTION_TOPIC, new CodeListener(tempProject));
            }
        }
    }

}
