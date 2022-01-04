package com.mromanak.dockercomposeintegrationtstst.controller;

import com.mromanak.dockercomposeintegrationtstst.model.dto.ItemRequestDto;
import com.mromanak.dockercomposeintegrationtstst.model.dto.ItemResponseDto;
import com.mromanak.dockercomposeintegrationtstst.model.jpa.Item;
import com.mromanak.dockercomposeintegrationtstst.repository.ItemRepository;
import com.mromanak.dockercomposeintegrationtstst.service.DtoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/repository/item")
public class ItemController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

    private final ItemRepository repository;
    private final DtoService dtoService;

    public ItemController(ItemRepository repository, DtoService dtoService) {
        this.repository = repository;
        this.dtoService = dtoService;
    }

    @RequestMapping(method = RequestMethod.GET)
    @Transactional
    public ResponseEntity<Page<ItemResponseDto>> getItemPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "25") int pageSize
    ) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.ASC, "name"));
        return ResponseEntity.ok(repository.findAll(pageRequest).map(dtoService::itemToResponseDto));
    }

    @RequestMapping(path = "/{itemId}", method = RequestMethod.GET)
    @Transactional
    public ResponseEntity<ItemResponseDto> getItem(
            @PathVariable(name = "itemId") Long itemId
    ) {
        return repository.findById(itemId).
                map(dtoService::itemToResponseDto).
                map(ResponseEntity::ok).
                orElseGet(() -> ResponseEntity.notFound().build());
    }

    @RequestMapping(path = "/{itemId}", method = RequestMethod.POST)
    @Transactional
    public ResponseEntity<ItemResponseDto> saveItem(
            @PathVariable(name = "itemId") Long itemId,
            @RequestBody @Valid ItemRequestDto itemDto
    ) {
        repository.save(dtoService.itemFromRequestDto(itemId, itemDto));
        return getItem(itemId);
    }

    @RequestMapping(path = "/{itemId}", method = RequestMethod.DELETE)
    @Transactional
    public ResponseEntity<ItemResponseDto> deleteItem(
            @PathVariable(name = "itemId") Long itemId
    ) {
        repository.deleteById(itemId);
        return ResponseEntity.noContent().build();
    }
}
