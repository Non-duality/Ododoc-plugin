package com.ssafy.ododocintellij.directory.dto.response;

import java.util.ArrayList;
import java.util.List;

public class DirectoryDto {

    private Long id = -1L;
    private String name = "";
    private String type = "";
    private List<DirectoryDto> children = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<DirectoryDto> getChildren() {
        return children;
    }

    public void setChildren(List<DirectoryDto> children) {
        this.children = children;
    }

}
