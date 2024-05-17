package com.ssafy.ododocintellij.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModifiedFileInfo {

    @JsonProperty
    private String fileName;
    @JsonProperty
    private String sourceCode;

    public ModifiedFileInfo(String fileName, String sourceCode){
        this.fileName = fileName;
        this.sourceCode = sourceCode;
    }

    @Override
    public String toString() {
        return fileName + " " + sourceCode;
    }
}
