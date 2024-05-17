package com.ssafy.ododocintellij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.ssafy.ododocintellij.directory.frame.DirectoryFrame;
import com.ssafy.ododocintellij.login.frame.MainLoginFrame;
import com.ssafy.ododocintellij.login.manager.TokenManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.jvnet.winp.Main;

public class OpenOdodocPlugin extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Platform.runLater(() -> {
            if(TokenManager.getInstance().getAccessToken() == null || TokenManager.getInstance().getRefreshToken() == null){
                if(!MainLoginFrame.isFrameVisible()){
                    new MainLoginFrame();
                }
            }
            else {
                new DirectoryFrame().start(new Stage());
            }
        });
    }
}
