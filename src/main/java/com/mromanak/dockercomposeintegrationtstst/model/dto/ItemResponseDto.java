package com.mromanak.dockercomposeintegrationtstst.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ItemResponseDto {

    private Long id;

    private String name;

    private String description;
}
