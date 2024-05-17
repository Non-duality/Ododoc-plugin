package com.ssafy.ododocintellij.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorFileInfo {
    @JsonProperty
    private String fileName;
    @JsonProperty
    private String sourceCode;
    @JsonProperty
    private int line;

    public ErrorFileInfo(String fileName, String sourceCode, int line) {
        this.fileName = fileName;
        this.sourceCode = sourceCode;
        this.line = line;
    }
}
