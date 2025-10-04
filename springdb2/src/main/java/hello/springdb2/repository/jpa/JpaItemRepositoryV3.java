package hello.springdb2.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import hello.springdb2.domain.Item;
import hello.springdb2.domain.QItem;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;

import jakarta.persistence.EntityManager;

/**
 * QueryDsl 동작 원리
 * 
 * 1. 엔티티 클래스 작성 : @Entity
 * 
 * 2. Q클래스(Q-Type) 생성
 * - QueryDsl은 "컴파일 시점"에 엔티티 기반으로 "Q클래스"를 자동 생성함
 * - Item -> QItem.java
 * - 이 Q클래스는 타입 안전한 쿼리를 만들 수 있도록 돕는 도우미 클래스
 * 
 * 3. 생성 위치
 * - Gradle 빌드 시 'annotationProcessor'가 실행됨
 * - Q클래스는 보통 'build/generated/sources/annotationProcessor/java/main' 안에 생성됨
 * 
 * 4. generated, build
 * - build/ : Gradle이 빌드할 때 쓰는 모든 산출물이 모이는 디렉토리
 * - build/generated/... : annotationProcessor(=쿼리DSL)가 Q클래스를 생성하는 디렉토리
 * - src/main/java : 우리가 직접 작성하는 코드.
 * - 즉, QItem.java 같은 건 "build/generated/..." 안에 생기고,
 *   우리가 직접 관리하지 않고, 빌드할 때마다 자동 생성된다.
 * 
 * 5. 실제 사용 방식
 * - Q클래스를 import 해서 JPQL 대신 타입 안전한 코드 작성 가능.
 *      예: QItem item = QItem.item;
 *          queryFactory.selectFrom(item)
 *                      .where(item.itemName.eq("상품A"))
 *                      .fetch();
 *
 * - 여기서 QItem.item 은 "static 싱글톤 객체" (Querydsl이 미리 만들어둠)
 * 
 */

@Transactional
public class JpaItemRepositoryV3 implements ItemRepository {

    private final EntityManager entityManager;
    private final JPAQueryFactory queryFactory;

    public JpaItemRepositoryV3(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

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
    public void update(Long itemId, ItemUpdateDto updateParam) {
        Item findItem = entityManager.find(Item.class, itemId);
        findItem.setItemName(updateParam.getItemName());
        findItem.setPrice(updateParam.getPrice());
        findItem.setQuantity(updateParam.getQuantity());
    }

    @Override
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
        if (StringUtils.hasText(itemName)) {
            return item.itemName.like("%" + itemName + "%");
        }
        return null;
    }

    private BooleanExpression maxPrice(Integer maxPrice, QItem item) {
        if (maxPrice != null) {
            return item.price.loe(maxPrice);
        }
        return null;
    }

    @Override
    public void delete(Long id) {
        Item item = entityManager.find(Item.class, id);
        if (item != null) {
            entityManager.remove(item);
        }
    }
}
