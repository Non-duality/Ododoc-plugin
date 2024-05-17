package com.ssafy.ododocintellij.tracker.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class BuildResultInfo {

    private boolean isSuccess;
    private long connectedFileId;
    private String contents;
    private String timeStamp;
    private List<ModifiedFileInfo> modifiedFiles;

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public long getConnectedFileId() {
        return connectedFileId;
    }

    public void setConnectedFileId(long connectedFileId) {
        this.connectedFileId = connectedFileId;
    }

    public List<ModifiedFileInfo> getModifiedFiles() {
        return modifiedFiles;
    }

    public void setModifiedFiles(List<ModifiedFileInfo> modifiedFiles) {
        this.modifiedFiles = modifiedFiles;
    }

}
