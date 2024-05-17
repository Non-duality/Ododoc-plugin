package com.ssafy.ododocintellij.directory.frame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.ododocintellij.directory.dto.request.ModifyRequestDto;
import com.ssafy.ododocintellij.directory.dto.response.ResultDto;
import com.ssafy.ododocintellij.directory.entity.FileInfo;
import com.ssafy.ododocintellij.login.alert.AlertHelper;
import com.ssafy.ododocintellij.login.frame.MainLoginFrame;
import com.ssafy.ododocintellij.login.manager.TokenManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

public class FileAndFolderTreeCell extends TreeCell<FileInfo> {

    private TextField textField;
    private Runnable refreshCallback;
    private ContextMenu folderContextMenu;
    private ContextMenu fileContextMenu;
    private Stage stage;
    private final String baseUrl = "https://k10d209.p.ssafy.io/api/directory";

    public FileAndFolderTreeCell(ContextMenu folderContextMenu, ContextMenu fileContextMenu, Stage stage,Runnable refreshCallback) {
        this.fileContextMenu = fileContextMenu;
        this.folderContextMenu = folderContextMenu;
        this.stage = stage;
        this.refreshCallback = refreshCallback;
    }

    @Override
    protected void updateItem(FileInfo fileInfo, boolean empty) {
        super.updateItem(fileInfo, empty);
        if(empty || fileInfo == null) {
            setText(null);
            setGraphic(null);
            setContextMenu(null);
        } else{
            if(isEditing()){
                if(textField != null){
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            }else{
                setText(fileInfo.toString());
                setGraphic(null);

                if(fileInfo.getType().equals("FOLDER")){
                    setContextMenu(folderContextMenu);
                }
                else if (fileInfo.getType().equals("FILE")){
                    setContextMenu(fileContextMenu);
                }
            }
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if(textField == null){
            createTextField();
        }
        setGraphic(textField);
        setText(null);
        textField.setText(getItem().getName());
        textField.selectAll();
        textField.requestFocus();
    }

    @Override
    public void commitEdit(FileInfo fileInfo) {
        super.commitEdit(fileInfo);
        modifyFolderOrFile(fileInfo.getName());
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem().getName());
        setGraphic(null);
    }

    private void createTextField() {
        textField = new TextField(getString());
        textField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                commitEdit(new FileInfo(getItem().getId(), textField.getText(), getItem().getType()));
            } else if (event.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
    }

    private void modifyFolderOrFile(String name) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-type", "application/json")
                .defaultHeader("Authorization", TokenManager.getInstance().getAccessToken())
                .build();

        ModifyRequestDto createRequestDto = new ModifyRequestDto(getItem().getId(), name);

        webClient.put()
                .uri("/edit")
                .bodyValue(createRequestDto)
                .retrieve()
                .bodyToMono(ResultDto.class)
                .subscribe(result -> {
                    if (result.getStatus() == 200) {
                        if (refreshCallback != null) {
                            refreshCallback.run();
                        }
                    }
                    else if (result.getStatus() == 401){
                        refreshAccessToken();
                        showAlert("수정 실패", "다시 시도해주세요.");
                        refreshCallback.run();
                    }
                    else {
                        showAlert("수정 실패", "다시 시도해주세요.");
                        refreshCallback.run();
                    }
                }, error ->{
                    showAlert("수정 실패", "다시 시도해주세요.");
                    refreshCallback.run();
                });
    }
    private void showAlert(String header, String content){
        Platform.runLater(() ->{
            Alert alert = AlertHelper.makeAlert(
                    Alert.AlertType.WARNING,
                    "디렉토리",
                    header,
                    content,
                    "/image/button/icon.png"
            );
            alert.showAndWait();
        });
    }

    private void refreshAccessToken() {
        WebClient webClient = WebClient.builder()
                .baseUrl("https://k10d209.p.ssafy.io/api")
                .defaultCookie("refreshToken", TokenManager.getInstance().getRefreshToken())
                .build();

        webClient.post()
                .uri("/oauth2/issue/access-token")
                .retrieve()
                .bodyToMono(ResultDto.class)
                .subscribe(result -> {
                    if (result.getStatus() == 200) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        Map<String, String> data = objectMapper.convertValue(result.getData(), Map.class);
                        TokenManager.getInstance().setAccessToken(data.get("accessToken"));
                    } else {
                        reLogin();
                    }
                }, error -> reLogin());

    }

    private void reLogin() {
        Platform.runLater(() -> {
            TokenManager.getInstance().setAccessToken(null);
            TokenManager.getInstance().setRefreshToken(null);

            stage.close();
            MainLoginFrame mainLoginFrame = new MainLoginFrame();
            mainLoginFrame.show();
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }
}
