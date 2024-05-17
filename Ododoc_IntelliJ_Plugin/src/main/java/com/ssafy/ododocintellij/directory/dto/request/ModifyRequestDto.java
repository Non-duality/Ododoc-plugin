package com.ssafy.ododocintellij.directory.dto.request;

public class ModifyRequestDto {

    private Long id;
    private String name;

    public ModifyRequestDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
