package hello.springdb2.v2;

import java.util.List;

import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import hello.springdb2.domain.Item;
import hello.springdb2.domain.QItem;
import hello.springdb2.repository.ItemSearchCond;
import jakarta.persistence.EntityManager;

public class ItemQueryRepositoryV2 {

	private final JPAQueryFactory queryFactory;
	
	public ItemQueryRepositoryV2(EntityManager entityManager) {
		this.queryFactory = new JPAQueryFactory(entityManager);
	}
	
	public List<Item> findAll(ItemSearchCond cond) {
		String itemName = cond.getItemName();
		Integer maxPrice = cond.getMaxPrice();
		
		QItem item = QItem.item;
		
		return queryFactory
				.select(item)
				.from(item)
				.where(likeItemName(itemName, item), maxPrice(maxPrice, item))
				.fetch();
	}
	
	private BooleanExpression likeItemName(String itemName, QItem item) {
		if(StringUtils.hasText(itemName)) {
			return item.itemName.like("%" + itemName + "%");
		} 
		return null;
	}
	
	private BooleanExpression maxPrice(Integer maxPrice, QItem item) {
		if(maxPrice != null) {
			return item.price.loe(maxPrice);
		}
		 return null;
	}
	
}
