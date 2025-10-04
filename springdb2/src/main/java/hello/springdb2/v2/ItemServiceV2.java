package hello.springdb2.v2;

import java.util.List;
import java.util.Optional;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;
import hello.springdb2.service.ItemService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional
@RequiredArgsConstructor
public class ItemServiceV2 implements ItemService {

	private final ItemRepositoryV2 itemRepositoryV2;
	private final ItemQueryRepositoryV2 itemQueryRepositoryV2;
	
	@Override
	public Item save(Item item) {
		return itemRepositoryV2.save(item);
	}
	
	@Override
	public Optional<Item> findById(Long id) {
		return itemRepositoryV2.findById(id);
	}
	
	@Override
	public List<Item> findItems(ItemSearchCond cond) {
		return itemQueryRepositoryV2.findAll(cond);
	}
	
	@Override
	public void update(Long itemId, ItemUpdateDto updateParam) {
		Item findItem = itemRepositoryV2.findById(itemId).orElseThrow();
		findItem.setItemName(updateParam.getItemName());
		findItem.setPrice(updateParam.getPrice());
		findItem.setQuantity(updateParam.getQuantity());
	}
	
}
