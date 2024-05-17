package com.ssafy.ododocintellij.directory.frame;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.ododocintellij.directory.dto.request.CreateRequestDto;
import com.ssafy.ododocintellij.directory.dto.response.DirectoryDto;
import com.ssafy.ododocintellij.directory.dto.response.ResultDto;
import com.ssafy.ododocintellij.directory.entity.FileInfo;
import com.ssafy.ododocintellij.directory.manager.ConnectedFileManager;
import com.ssafy.ododocintellij.directory.manager.DirectoryInfoManager;
import com.ssafy.ododocintellij.login.alert.AlertHelper;
import com.ssafy.ododocintellij.login.frame.MainLoginFrame;
import com.ssafy.ododocintellij.login.manager.TokenManager;
import com.ssafy.ododocintellij.sender.BuildResultSender;
import com.ssafy.ododocintellij.sender.alert.WebSocketReConnectAlert;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DirectoryFrame extends Application {

    private Long currentDirectoryId;
    private final String baseUrl = "https://k10d209.p.ssafy.io/api/directory";
    private TreeView<FileInfo> treeView;
    private ContextMenu folderContextMenu = new ContextMenu();
    private ContextMenu fileContextMenu = new ContextMenu();
    private Stage stage;

    @Override
    public void start(Stage initStage) {
        DirectoryInfoManager directoryInfoManager = DirectoryInfoManager.getInstance();
        ResultDto resultDto = retrieveDirectory(directoryInfoManager.getRootId()).block();
        this.stage = initStage;
        // 제목 설정
        stage.setTitle(" " + directoryInfoManager.getTitle());
        currentDirectoryId = directoryInfoManager.getRootId();

        // 오른쪽 마우스 이벤트 목록 생성
        makeContextMenu();

        // 디렉토리 UI 생성
        TreeItem<FileInfo> invisibleRoot = new TreeItem<>();
        invisibleRoot = LoadDirectory(((DirectoryDto)resultDto.getData()).getChildren(), invisibleRoot);

        treeView = new TreeView<>(invisibleRoot);
        treeView.setShowRoot(false);
        treeView.setEditable(true);
        treeView.setCellFactory(tv -> new FileAndFolderTreeCell(folderContextMenu, fileContextMenu, stage, this::refreshDirectoryView));
        treeView.setOnMouseClicked(event -> {
            // 오른쪽 마우스 클릭 시 빈 공간 일 경우 파일 및 폴더 생성
            if (event.getButton() == MouseButton.SECONDARY) {
                if(treeView.getSelectionModel().getSelectedItem() == null){
                    folderContextMenu.show(treeView, event.getScreenX(), event.getScreenY());
                    currentDirectoryId = directoryInfoManager.getRootId();
                } else{
                    currentDirectoryId = treeView.getSelectionModel().getSelectedItems().get(0).getValue().getId();
                }
            } else {
                folderContextMenu.hide();
            }

            // 왼쪽 마우스 클릭 시
            if(event.getButton() == MouseButton.PRIMARY){
                // 빈공간일 경우 폴더 및 파일을 선택 비활성화
                if(event.getTarget() instanceof TreeCell<?> && ((TreeCell) event.getTarget()).isEmpty()){
                    treeView.getSelectionModel().clearSelection();
                    currentDirectoryId = directoryInfoManager.getRootId();
                }
                else{
                    if(treeView.getSelectionModel().getSelectedItems().isEmpty()){
                        currentDirectoryId = directoryInfoManager.getRootId();
                    }
                    else{
                        currentDirectoryId = treeView.getSelectionModel().getSelectedItems().get(0).getValue().getId();
                    }
                }
            }
        });

        Button refreshButton = new Button();
        ImageView refreshIcon = new ImageView(new Image(getClass().getResourceAsStream("/image/button/refresh.png")));
        refreshButton.setGraphic(refreshIcon);
        refreshButton.setTooltip(new Tooltip("새로고침"));
        refreshButton.setOnAction(e -> refreshDirectoryView());

        Button connectButton = new Button();
        ImageView connectIcon = new ImageView(new Image(getClass().getResourceAsStream("/image/button/connect.png")));
        connectButton.setGraphic(connectIcon);
        connectButton.setTooltip(new Tooltip("서버 연결"));
        connectButton.setOnAction(e -> connectWebSocket());

        Button homeButton = new Button();
        ImageView homeIcon = new ImageView(new Image(getClass().getResourceAsStream("/image/button/home.png")));
        homeButton.setGraphic(homeIcon);
        homeButton.setTooltip(new Tooltip("Ododoc 홈페이지로 이동"));
        homeButton.setOnAction(e -> openWebPage());

        ToolBar toolBar = new ToolBar();
        toolBar.getItems().addAll(homeButton, connectButton, refreshButton);
        BorderPane root = new BorderPane();
        root.setBottom(toolBar);
        root.setCenter(treeView);


        Scene scene = new Scene(root, 300, 500);
        stage.setScene(scene);
        stage.show();
    }

    private Mono<ResultDto> retrieveDirectory(long rootId) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-type", "application/json")
                .defaultHeader("Authorization", TokenManager.getInstance().getAccessToken())
                .build();

        return webClient.get()
                .uri("/" + rootId)
                .retrieve()
                .bodyToMono(ResultDto.class)
                .doOnSuccess(result -> {
                    // 성공했을 경우
                    if(result.getStatus() == 200){
                        ObjectMapper objectMapper = new ObjectMapper();
                        result.setData(objectMapper.convertValue(result.getData(), DirectoryDto.class));
                    }
                    // 토큰이 만료되었을 경우
                    else if(result.getStatus() == 401){
                        result.setData(new DirectoryDto());
                        refreshAccessToken();
                        retrieveDirectory(DirectoryInfoManager.getInstance().getRootId());
                    }
                    else{
                        result.setData(new DirectoryDto());
                        showAlert(Alert.AlertType.WARNING,"조회 실패", "디렉토리 조회에 실패했습니다.\n 새로고침 버튼은 눌러 다시 시도해주세요.");
                    }
                })
                .doOnError(error -> showAlert(Alert.AlertType.WARNING,"조회 실패", "디렉토리 조회에 실패했습니다.\n 새로고침 버튼은 눌러 다시 시도해주세요."));
    }

    private TreeItem<FileInfo> LoadDirectory(List<DirectoryDto> children, TreeItem<FileInfo> invisibleRoot) {

        for(DirectoryDto dto : children){
            FileInfo fileInfo = new FileInfo(dto.getId(), dto.getName(), dto.getType());
            TreeItem<FileInfo> fileItem = new TreeItem<>(fileInfo);
            fileItem.setExpanded(true);
            DFS(dto.getChildren(), fileItem);
            invisibleRoot.getChildren().add(fileItem);
        }

        return invisibleRoot;
    }

    private void DFS(List<DirectoryDto> children, TreeItem<FileInfo> fileItem) {

        if(children == null){
            return;
        }

        for(DirectoryDto dto : children){
            FileInfo fileInfo = new FileInfo(dto.getId(), dto.getName(), dto.getType());
            TreeItem<FileInfo> childFileItem = new TreeItem<>(fileInfo);
            fileItem.getChildren().add(childFileItem);

            DFS(dto.getChildren(), childFileItem);
        }

    }

    private void makeContextMenu() {
        MenuItem addFolder = new MenuItem("폴더 생성");
        MenuItem addFile = new MenuItem("파일 생성");
        folderContextMenu.getItems().addAll(addFolder, addFile);

        MenuItem connectFile = new MenuItem("파일 연동");
        fileContextMenu.getItems().add(connectFile);

        addFolder.setOnAction(e -> createFolderOrFile("folder"));
        addFile.setOnAction(e -> createFolderOrFile("file"));
        connectFile.setOnAction(e -> connectFile());
    }

    private void connectFile() {
        ConnectedFileManager connectedFileManager = ConnectedFileManager.getInstance();
        connectedFileManager.setDirectoryId(currentDirectoryId);

        if(connectedFileManager.getDirectoryId() != -1){
            showAlert(Alert.AlertType.INFORMATION, "연동 성공", "파일과 연동에 성공하였습니다.");
        }
        else{
            showAlert(Alert.AlertType.WARNING, "연동 실패", "파일과의 연동에 실패하였습니다.");
        }
    }

    private void createFolderOrFile(String type){
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Content-type", "application/json")
                .defaultHeader("Authorization", TokenManager.getInstance().getAccessToken())
                .build();

        CreateRequestDto createRequestDto = new CreateRequestDto(currentDirectoryId, "", type);

        webClient.post()
                .bodyValue(createRequestDto)
                .retrieve()
                .bodyToMono(ResultDto.class)
                .subscribe(result -> {
                    if (result.getStatus() == 200) {
                        refreshDirectoryView();
                    }
                    else if (result.getStatus() == 401) {
                        refreshAccessToken();
                        showAlert(Alert.AlertType.WARNING, "생성 실패", "다시 시도해주세요.");
                        refreshDirectoryView();
                    }
                    else {
                        showAlert(Alert.AlertType.WARNING,"생성 실패", "다시 시도해주세요.");
                        refreshDirectoryView();
                    }
                }, error -> {
                    showAlert(Alert.AlertType.WARNING,"생성 실패", "다시 시도해주세요.");
                    refreshDirectoryView();
                });
    }

    private void refreshDirectoryView() {
        retrieveDirectory(DirectoryInfoManager.getInstance().getRootId()).subscribe(resultDto -> {
            Platform.runLater(() -> {
                TreeItem<FileInfo> invisibleRoot = new TreeItem<>();
                invisibleRoot = LoadDirectory(((DirectoryDto)resultDto.getData()).getChildren(), invisibleRoot);
                treeView.setRoot(invisibleRoot);
                treeView.setShowRoot(false);
                treeView.refresh();
            });
        });
    }

    private void showAlert(Alert.AlertType type, String header, String content){
        Platform.runLater(() ->{
            Alert alert = AlertHelper.makeAlert(
                    type,
                    " Ododoc",
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

    private void connectWebSocket(){
        if(BuildResultSender.isConnected()){
            Platform.runLater(() -> {
                Alert alert = AlertHelper.makeAlert(
                        Alert.AlertType.INFORMATION,
                        " Ododoc",
                        "서버 연결 확인",
                        "이미 서버와 연결이 되어 있습니다.",
                        "/image/button/icon.png"
                );
                alert.showAndWait();
            });
        }

        else {
            Platform.runLater(() -> {
                Alert alert = WebSocketReConnectAlert.makeAlert();
                Optional<ButtonType> result = alert.showAndWait();
                if(result.isPresent() && result.get() == ButtonType.OK) {
                    BuildResultSender.setEnableWhenPushBtn(true);
                    BuildResultSender.setINSTANCE(null);
                    BuildResultSender.getINSTANCE();
                }
            });
        }

    }

    private void openWebPage(){
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

}
