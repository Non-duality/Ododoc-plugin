package com.ssafy.ododocintellij.directory.dto.response;

public class ResultDto {

    private int status;
    private Object data;

    public int getStatus() {
        return status;
    }

    public Object getData() {
        return data;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
