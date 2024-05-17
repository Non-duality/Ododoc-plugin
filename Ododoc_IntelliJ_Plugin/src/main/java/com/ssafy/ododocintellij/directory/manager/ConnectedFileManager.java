package com.ssafy.ododocintellij.directory.manager;

public class ConnectedFileManager {

    private Long directoryId = 0L;

    private ConnectedFileManager() {}

    private static class ConnectedFileManagerHolder {
        private static final ConnectedFileManager INSTANCE = new ConnectedFileManager();
    }

    public static ConnectedFileManager getInstance() {
        return ConnectedFileManager.ConnectedFileManagerHolder.INSTANCE;
    }

    public Long getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(Long directoryId) {
        this.directoryId = directoryId;
    }
}
