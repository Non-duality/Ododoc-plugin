package com.ssafy.ododocintellij.tracker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RequestDto {
    @JsonProperty
    private String sourceApplication;
    @JsonProperty
    private String accessToken;
    @JsonProperty
    private String dataType;
    @JsonProperty
    private Long connectedFileId;
    @JsonProperty
    private String timestamp;
    @JsonProperty
    private Object content;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    public void setSourceApplication(String sourceApplication) {
        this.sourceApplication = sourceApplication;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setConnectedFileId(Long connectedFileId) {
        this.connectedFileId = connectedFileId;
    }


    public void setContent(Object content) {
        this.content = content;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp.format(formatter);
    }

}
