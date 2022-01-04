package com.mromanak.dockercomposeintegrationtstst.repository;

import com.mromanak.dockercomposeintegrationtstst.model.jpa.Item;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {
}
