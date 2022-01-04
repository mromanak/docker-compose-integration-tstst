package com.mromanak.dockercomposeintegrationtstst.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ItemRequestDto {

    @NotBlank(message = "Name must be non-blank")
    private String name;

    private String description;
}
