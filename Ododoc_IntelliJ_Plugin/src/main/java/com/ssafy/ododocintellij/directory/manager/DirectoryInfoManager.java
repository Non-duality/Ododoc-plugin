package com.ssafy.ododocintellij.directory.manager;

public class DirectoryInfoManager {

    private long rootId;
    private String title;

    private DirectoryInfoManager() {}

    private static class DirectoryInfoManagerHolder {
        private static final DirectoryInfoManager INSTANCE = new DirectoryInfoManager();
    }

    public static DirectoryInfoManager getInstance() {
        return DirectoryInfoManagerHolder.INSTANCE;
    }

    public synchronized long getRootId() {
        return rootId;
    }
    public synchronized String getTitle() {
        return title;
    }

    public synchronized void setRootId(long rootId) {
        this.rootId = rootId;
    }

    public synchronized void setTitle(String title) {
        this.title = title;
    }
}
