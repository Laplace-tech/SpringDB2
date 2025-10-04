package hello.springdb2.repository;

import java.util.List;
import java.util.Optional;

import hello.springdb2.domain.Item;

/**
 * ItemRepository 인터페이스
 * 
 * - 상품(Item) 엔티티의 데이터 접근 계층(Repository)역할을 추상화한 인터페이스
 * - 구체적인 "구현 기술(JdbcTemplate, MyBatis, JPA 등)에 의존하지 않고"
 *   "인터페이스만" 바라보도록 설계 -> 구현체를 갈아끼우기 쉽다 (DI)
 * - JdbcTemplateItemRepository, MyBatisItemRepository, JpaItemRepository 등
 *   다양한 구현체로 대체 가능
 * - 서비스 계층(Service)은 구현체가 아닌 인터페이스(ItemRepository)에만 의존
 * 
 */

public interface ItemRepository {
	
	Item save(Item item);
	Optional<Item> findById(Long id);
	List<Item> findAll(ItemSearchCond cond);
	void update(Long itemId, ItemUpdateDto updateParam);
	void delete(Long id);
}
