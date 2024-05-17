package com.ssafy.ododocintellij.tracker.dto;

import java.util.List;

public class BuildResultInfo {

    private String details;
    private List<ModifiedFileInfo> modifiedFiles;
    private ErrorFileInfo errorFile;

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<ModifiedFileInfo> getModifiedFiles() {
        return modifiedFiles;
    }

    public void setModifiedFiles(List<ModifiedFileInfo> modifiedFiles) {
        this.modifiedFiles = modifiedFiles;
    }

    public ErrorFileInfo getErrorFile() {
        return errorFile;
    }

    public void setErrorFile(ErrorFileInfo errorFile) {
        this.errorFile = errorFile;
    }
}
