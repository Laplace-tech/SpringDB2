package hello.springdb2.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.util.StringUtils;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA 적용 원리 및 JpaItemRepository 매서드 동작 분석
 * 
 * 1. EntityManager
 * - JPA의 모든 동작은 EntityManager를 통해 수행됨
 * - EntityManager는 내부적으로 DataSource를 가지고 있으며 데이터베이스에 직접 접근할 수 있음
 * - 스프링에서는 생성자 주입(Constructor Injection)을 통해 EntityManager를 제공
 * 
 * 예시: 
 * private final EntityManager em;
 * public JpaItemRepositoryV1(EntityManager em) { this.em = em;}
 * 
 * 참고: Spring Boot는 EntityManagerFactory, JpaTransactionManager 등
 *      JPA 설정을 자동을 설정함
 * 
 * 2. 트랜잭션 처리 : @Transactional
 * - JPA에서 데이터 변경(등록, 수정, 삭제)는 데이터 일관성과 성능을 위해
 *   반드시 트랜잭션 안에서 이루어져야 한다. 일반적으로 트랜잭션은 서비스 계층에서 선언한다.
 * -----------------------------------------------------------------------
 * 예외 변환(Exception Translation) - JPA
 *
 * 1. 문제 상황
 *    - JPA(EntityManager)를 사용하면 내부에서 문제가 발생했을 때
 *      JPA 전용 예외(PersistenceException 및 하위 예외)를 던짐.
 *    - 예: em.persist(item) 수행 시 DB 오류 발생 → PersistenceException 발생.
 *    - 이러한 예외들은 스프링의 공통 예외 계층(DataAccessException)과 다름.
 *    - 결과적으로 서비스 계층에서 일관된 예외 처리가 어려움.
 *
 * 2. 스프링의 해결책
 *    - 스프링은 PersistenceExceptionTranslator를 통해
 *      JPA 예외를 DataAccessException으로 변환하는 기능 제공.
 *    - 이를 "예외 변환(Exception Translation)"이라고 함.
 *
 * 3. 동작 과정
 *    1) @Repository 애노테이션 사용
 *       - 스프링에게 해당 클래스가 데이터 접근 계층임을 알림.
 *       - 예외 변환 AOP 대상이 됨.
 *
 *    2) AOP 프록시 생성
 *       - 스프링이 자동으로 리포지토리 클래스에 프록시를 생성.
 *       - 메서드 호출 시 예외를 가로챔.
 *
 *    3) 예외 변환 수행
 *       - JPA 예외 → 스프링 DataAccessException 변환.
 *       - 내부적으로 EntityManagerFactoryUtils.convertJpaAccessExceptionIfPossible() 사용.
 *
 *    4) 일관된 예외 처리 가능
 *       - 서비스 계층에서 JPA인지 JDBC인지 구분 없이 동일하게 처리 가능.
 *
 * 4. 스프링 부트 지원
 *    - PersistenceExceptionTranslationPostProcessor를 자동 등록.
 *    - @Repository가 붙은 클래스에 자동으로 예외 변환 기능 적용.
 *
 * @Repository를 사용하면 JPA 예외를 자동으로 스프링 DataAccessException으로 변환하여
 * 예외 처리를 일관되고 기술에 독립적으로 할 수 있습니다.
 */

@Slf4j
@Transactional
@RequiredArgsConstructor
public class JpaItemRepositoryV1 implements ItemRepository {

	private final EntityManager entityManager;
	
	@Override
	public Item save(Item item) {
		entityManager.persist(item);
		return item;
	}

	@Override
	public Optional<Item> findById(Long id) {
		Item item = entityManager.find(Item.class, id);
		return Optional.ofNullable(item);
	}

	@Override
	public List<Item> findAll(ItemSearchCond cond) {
		String jpql = "select i from Item i";
		boolean hasCondition = false;
		
		String itemName = cond.getItemName();
		Integer maxPrice = cond.getMaxPrice();

		if (StringUtils.hasText(itemName) || maxPrice != null) {
			jpql += " where";
		}

		if (StringUtils.hasText(itemName)) {
			jpql += " i.itemName like concat('%', :itemName, '%')";
			hasCondition = true;
		}

		if (maxPrice != null) {
			if (hasCondition) {
				jpql += " and";
			} else {
				jpql += " i.price <= :maxPrice";
			}
		}

		log.info("JPQL = {}, itemName = {}, maxPrice = {}", jpql, itemName, maxPrice);

		TypedQuery<Item> query = entityManager.createQuery(jpql, Item.class);
		
		if (StringUtils.hasText(itemName)) {
			query.setParameter("itemName", itemName);
		}
		
		if(maxPrice != null) {
			query.setParameter("maxPrice", maxPrice);
		}
		
		return query.getResultList();
	}

	@Override
	public void update(Long itemId, ItemUpdateDto updateParam) {
		Item findItem = entityManager.find(Item.class, itemId);
		findItem.setItemName(updateParam.getItemName());
		findItem.setPrice(updateParam.getPrice());
		findItem.setQuantity(updateParam.getQuantity());
	}

	@Override
	public void delete(Long id) {
		Item item =  entityManager.find(Item.class, id);
		entityManager.remove(item);
	}

}
