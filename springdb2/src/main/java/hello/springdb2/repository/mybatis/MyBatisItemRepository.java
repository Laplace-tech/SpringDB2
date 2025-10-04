package hello.springdb2.repository.mybatis;

import java.util.List;
import java.util.Optional;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MyBatis 개념 정리
 * 
 * 1. MyBatis
 * - MyBatis는 JDBC, JdbcTemplate 보다 더 많은 기능을 제공하는 SQL Mapper 프레임워크
 * - SQL을 XML로 편리하게 작성할 수 있고, 동적 쿼리를 매우 쉽게 만들 수 있다
 * ------------------------------------------------------------------------
 * 2. JdbcTemplate vs MyBatis
 * 
 *  - SQL 여러 줄 처리
 *    JdbcTemplate:
 *      String sql = "update item " +
 *       			 "set item_name=:itemName, price=:price, quantity=:quantity " +
 *       			 "where id = :id"
 *    
 *    MyBatis:
 *      <update id="update">
 *        update item
 *        set item_name=#{itemName},
 *            price=#{price}
 *            quantity=#{quantity}
 *        where id=#{id}
 *      </update>
 * 
 *  - 동적 쿼리 처리
 *    JdbcTemplate:
 *      String sql = "select id, item_name, price, quantity from item"
 *      if(condition) {
 *      	sql += " where...";
 *      }
 *      
 *    MyBatis:
 *      <select id="findAll" resultType="Item">
 *      	select id, item_name, price, quantity
 *       	from item
 *       	<where>
 *       		<if test="itemName != null and itemName != ''">
 *       			item_name like concat('%', #{itemName}, '%')
 *       		</if>
 *       		<if test="maxPrice != null">
 *       			price &lt;= #{maxPrice}
 *       		</if> 
 *       	</where>
 *      </select>  
 * ------------------------------------------------------------------------
 * 3. 관례의 불일치
 * - Java 객체는 camelCase 표기법 사용: itemName
 * - DB 컬럼은 snake_case 표기법 사용: item_name
 * - map-underscore-to-camel-case 옵션 활성화 시 자동 변환 가능
 * - 컬럼과 객체명이 완전히 다르면 별칭 사용 필요
 *   예:
 *     select item_name as name
 * ------------------------------------------------------------------------
 *  # MyBatis 설정
 *  mybatis.type-aliases-package=hello.springdb2.domain
 *  - 지정한 패키지의 도메인 클래스명을 MyBatis에서 별칭으로 사용 가능
 *  
 *  mybatis.configuration.map-underscore-to-camel-case=true
 *  - DB 컬럼명 snake_case 자동 변환
 *
 *  logging.level.hello.itemservice.repository.mybatis=trace
 *  - MyBatis 실행 쿼리 로그 확인 가능  
 */

@Slf4j
@RequiredArgsConstructor
public class MyBatisItemRepository implements ItemRepository {

	/**
	 * 스프링이 itemMapper 프록시 객체를 자동으로 주입
	 * - ItemMapper는 인터페이스일 뿐인데, MyBatis가 동적 프록시 구현체를 만들어서 
	 *   스프링 빈으로 등록해 둠 (@MapperScan)
	 *   
	 * @MapperScan 
	 * - MyBatis에서 @Mapper 가 붙은 인터페이스를 자동으로 스캔해서
	 *   동적 프록시 객체를 만들어 스프링 빈으로 등록해주는 기능
	 */
	private final ItemMapper itemMapper;
	
	@Override
	public Item save(Item item) {
		log.info("itemMapper class = {}", itemMapper);
		itemMapper.save(item);
		return item;
	}

	@Override
	public Optional<Item> findById(Long id) {
		return itemMapper.findById(id);
	}

	@Override
	public List<Item> findAll(ItemSearchCond cond) {
		return itemMapper.findAll(cond);
	}

	@Override
	public void update(Long itemId, ItemUpdateDto updateParam) {
		itemMapper.update(itemId, updateParam);
	}

	@Override
	public void delete(Long id) {
		itemMapper.delete(id);
	}

}
