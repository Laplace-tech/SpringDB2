package hello.springdb2.repository.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hello.springdb2.domain.Item;

/**
 * JpaRepository 개념
 * - JpaRepository<T, ID> 인터페이스를 상속받으면 CRUD, 페이징, 정렬 기능 자동 제공
 * - T: 엔티티 타입, ID: 엔티티 PK타입
 * - 직접 구현 클래스 작성 없이 인터페이스 선언만으로 스프링이 런타임에 구현체 생성
 * 
 * 쿼리 메서드(Query Methods)
 * - 메서드 이름을 분석해 자동으로 JPQL 쿼리를 생성
 * - 예시:
 * 	- findAll(): 모든 데이터 조회
 *    JPQL: select i from Item i
 *    
 *  - findByItemNameLike(String itemName): 이름 검색
 *    JPQL: select i from Item i where i.itemName like ?
 *    
 *  - findByPriceLessThanEqual(Integer price): 가격 조건 검색
 *    JPQL: select i from Item i where i.price <= ?
 *    
 *  - findByItemNameLikeAndPriceLessThanEqual(String itemName, Integer price): 이름 + 가격 조건 검색
 *    JPQL: select i from Item i where i.itemName like ? and i.price <= ?
 *    
 * @Query 애너테이션
 * - 직접 JPQL 또는 네이티브 쿼리 작성 가능
 * - 예시:
 *   @Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
 *   List<Item> findItems(@Param("itemName") String itemName, @Param("price") Integer price);
 *   
 */

public interface SpringDataJpaItemRepository extends JpaRepository<Item, Long>{

	List<Item> findByItemNameLike(String itemName);
	
	List<Item> findByPriceLessThanEqual(Integer price);
	
	List<Item> findByItemNameLikeAndPriceLessThanEqual(String itemName, Integer price);

	@Query("select i from Item i where i.itemName like :itemName and i.price <= :price")
	List<Item> findItems(@Param("itemName") String itemName, @Param("price") Integer price);
}
