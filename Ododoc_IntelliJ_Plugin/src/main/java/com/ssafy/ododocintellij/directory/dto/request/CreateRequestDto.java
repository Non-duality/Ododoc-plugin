package com.ssafy.ododocintellij.directory.dto.request;

public class CreateRequestDto {

    private Long parentId;
    private String name;
    private String type;

    public CreateRequestDto(Long parentId, String name, String type) {
        this.parentId = parentId;
        this.name = name;
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
