package com.mromanak.dockercomposeintegrationtstst.service;

import com.mromanak.dockercomposeintegrationtstst.model.dto.ItemRequestDto;
import com.mromanak.dockercomposeintegrationtstst.model.dto.ItemResponseDto;
import com.mromanak.dockercomposeintegrationtstst.model.jpa.Item;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class DtoService {

    public Item itemFromRequestDto(Long id, ItemRequestDto itemDto) {
        Objects.requireNonNull(id, "ID must be non-null");
        if (itemDto == null) {
            return null;
        }

        Item item = new Item();
        item.setId(id);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        return item;
    }

    public ItemResponseDto itemToResponseDto(Item item) {
        if (item == null) {
            return null;
        }

        ItemResponseDto itemDto = new ItemResponseDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        return itemDto;
    }
}
