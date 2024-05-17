package com.ssafy.ododocintellij.tracker.response;

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

}
