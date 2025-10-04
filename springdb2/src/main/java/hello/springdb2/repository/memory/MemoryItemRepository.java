package hello.springdb2.repository.memory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.util.ObjectUtils;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemoryItemRepository implements ItemRepository {

	private static final ConcurrentHashMap<Long, Item> store = new ConcurrentHashMap<>();
	private static final AtomicLong sequence = new AtomicLong(0L);
	
	@Override
	public Item save(Item item) {
		long sequenceId = sequence.incrementAndGet();
		item.setId(sequenceId);
		store.put(sequenceId, item);
		return item;
	}

	@Override
	public Optional<Item> findById(Long id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public List<Item> findAll(ItemSearchCond cond) {
		String itemName = cond.getItemName();
		Integer maxPrice = cond.getMaxPrice();
		return store.values().stream().filter(item -> {
			if (ObjectUtils.isEmpty(itemName)) {
				return true;
			}
			return item.getItemName().contains(itemName);
		}).filter(item -> {
			if (maxPrice == null) {
				return true;
			}
			return item.getPrice() <= maxPrice;
		}).collect(Collectors.toList());
	}

	@Override
	public void update(Long itemId, ItemUpdateDto updateParam) {
		Item findItem = store.get(itemId);
		findItem.setItemName(updateParam.getItemName());
		findItem.setPrice(updateParam.getPrice());
		findItem.setQuantity(updateParam.getQuantity());
	}

	@Override
	public void delete(Long id) {
		store.remove(id);
	}
	
	public void clearStore() {
		store.clear();
	}

}
