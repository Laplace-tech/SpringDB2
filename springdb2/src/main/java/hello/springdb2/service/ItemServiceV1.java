package hello.springdb2.service;

import java.util.List;
import java.util.Optional;


import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ItemServiceV1 implements ItemService {
	
	private final ItemRepository itemRepository;
	
	@Override
	public Item save(Item item) {
		return itemRepository.save(item);
	}

	@Override
	public Optional<Item> findById(Long id) {
		return itemRepository.findById(id);
	}

	@Override
	public List<Item> findItems(ItemSearchCond cond) {
		return itemRepository.findAll(cond);
	}

	@Override
	public void update(Long itemId, ItemUpdateDto updateParam) {
		itemRepository.update(itemId, updateParam);
	}

}
