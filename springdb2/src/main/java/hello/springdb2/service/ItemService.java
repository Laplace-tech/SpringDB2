package hello.springdb2.service;

import java.util.List;
import java.util.Optional;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;

public interface ItemService {

	Item save(Item item);
	Optional<Item> findById(Long id);
	List<Item> findItems(ItemSearchCond cond);
	void update(Long itemId, ItemUpdateDto updateParam);
}
