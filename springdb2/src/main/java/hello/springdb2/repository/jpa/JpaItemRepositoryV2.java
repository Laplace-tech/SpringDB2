package hello.springdb2.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.util.StringUtils;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * SpringDataJPA_ConcepNotes
 * 
 * 1) 주요 인터페이스 계층
 * 
 * CrudRepository<T, ID> 
 * - 가장 기본이 되는 CRUD 인터페이스 
 * - save, findById, existsById, findAll, count, deleteById 등 제공 
 * - 기존의 findOne()은 findById()로 변경 (리턴 타입 Optional 사용)
 * 
 * PagingAndSortingRepository<T, ID> 
 * - CrudRepository를 확장하여 페이징(Paging)과 정렬(Sorting) 기능 추가 
 * - 반환 타입: Page<T>, Slice<T>
 * 
 * JpaRepository<T, ID>
 * - JPA 특화 기능을 더한 인터페이스
 * - flush(), saveAndFlush, deleteInBatch(), getOne(지연 조회; 현재는 getById 권장) 등 추가
 * - 실무에서는 대부분 JpaRepository를 상속해서 사용
 * ------------------------------------------------------------------------- 
 * 2) 사용 예 (간단한 선언)
 * 
 * public interface ItemRepository extends JpaRepository<Item, Long> { }
 * 
 * - 제네릭: <엔티티 타입, ID 타입> 
 * - Spring Data JPA가 "런타임"에 이 "인터페이스의 구현체(프록시)"를 만들어 "스프링 빈"으로 등록한다. 
 * - 따라서 개발자는 직접 구현체를 만들 필요가 없다(단, 추가 커스텀 구현은 가능).
 * ------------------------------------------------------------------------- 
 * 3) 쿼리 생성 방식
 * 
 * 1. 메서드 이름으로 쿼리 유추 (Derived Query) 
 * 예: List<Item> findByItemNameContainingAndPriceLessThanEqual(String name, Integer maxPrice) 
 * - 이름을 분석해 WHERE 절을 만들기 때문에 간단한 조건에는 아주 편리 
 * - 복잡한 쿼리는 가독성 저하 또는 한계 발생
 *
 * 2. @Query 어노테이션 (JPQL 또는 nativeQuery=true) 
 * - 복잡한 조인이나 집계, 성능 튜닝이 필요할 때 사용 
 * 예: @Query("select i from Item i where i.price >= :min")
 *
 * -------------------------------------------------------------------------
 * 4) Spring 이 구현체를 생성하는 방법(개념)
 * 
 * - 스프링 부트/스프링 설정에서 @EnableJpaRepositories 또는 Spring Boot의 자동설정이 
 *   Repository 인터페이스를 스캔한다. 
 * - RepositoryFactorySupport (구현체: JpaRepositoryFactory 등)가 인터페이스를 분석하고 
 *   SimpleJpaRepository 같은 기본 구현을 사용해 프록시 인스턴스를 생성한다. 
 * - 프록시는 내부적으로 EntityManager를 주입받아 실제 DB 연산을 수행한다. 
 * - 개발자는 "인터페이스"만 선언하면 스프링이 런타임에 "구현체"를 만들어 빈으로 등록한다.
 */


@Transactional
@RequiredArgsConstructor
public class JpaItemRepositoryV2 implements ItemRepository {

	private final SpringDataJpaItemRepository springDataJpaItemRepository;
	
	@Override
	public Item save(Item item) {
		return springDataJpaItemRepository.save(item);
	}

	@Override
	public Optional<Item> findById(Long id) {
		return springDataJpaItemRepository.findById(id);
	}


	@Override
	public List<Item> findAll(ItemSearchCond cond) {
		String itemName = cond.getItemName();
		Integer maxPrice = cond.getMaxPrice();
		
		String nameSearchCondition = "%" + itemName + "%";
		
		if(StringUtils.hasText(itemName) && maxPrice != null) {
//			return springDataJpaItemRepository.findByItemNameLikeAndPriceLessThanEqual(nameSearchCondition, maxPrice);
			return springDataJpaItemRepository.findItems(nameSearchCondition, maxPrice);
		} else if (StringUtils.hasText(itemName)) {
			return springDataJpaItemRepository.findByItemNameLike(nameSearchCondition);
		} else if (maxPrice != null) {
			return springDataJpaItemRepository.findByPriceLessThanEqual(maxPrice);
		} else {
			return springDataJpaItemRepository.findAll();
		}
	}


	@Override
	public void update(Long itemId, ItemUpdateDto updateParam) {
		Item findItem = springDataJpaItemRepository.findById(itemId).orElseThrow();
		findItem.setItemName(updateParam.getItemName());
		findItem.setPrice(updateParam.getPrice());
		findItem.setItemName(updateParam.getItemName());
	}

	@Override
	public void delete(Long id) {
		springDataJpaItemRepository.deleteById(id);
	}

	
}









