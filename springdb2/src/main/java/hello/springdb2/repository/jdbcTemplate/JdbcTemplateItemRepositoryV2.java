package hello.springdb2.repository.jdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;

/**
 * JdbcTemplateItemRepositoryV2
 * 
 * NamedParameterJdbcTemplate을 사용한 Repository 구현
 * - 순서 기반 파라미터 바인딩의 위험성 제거 (명시적 파라미터 이름 사용)
 * - 유지보수성이 높음 (SQL 수정 시 순서 오류 방지)
 * - BeanPropertySqlParameterSource를 통해 객체 필드와 SQL 파라미터 자동 매핑 가능
 * 
 * -----------------------------------------------------------------------
 * [파라미터 전달 방법 3가지]
 * 
 * 1) Map 사용 (단순 key-value)
 *   - Map<String, Object> param = Map.of("id", id);
 *     template.queryForObject(sql, param, itemRowMapper());
 * 
 * 2) MapSqlParameterSource (SQL 특화 Map)
 *   - SqlParameterSource 인터페이스 구현체
 *   - 체인 메서드 제공, SQL 타입 지정 가능
 *     SqlParameterSource param = new MapSqlParameterSource()
 *          .addValue("itemName", updateParam.getItemName())
 *          .addValue("price", updateParam.getPrice())
 *          .addValue("quantity", updateParam.getQuantity())
 *          .addValue("id", itemId);
 *     template.update(sql, param);
 *     
 * 3) BeanPropertySqlParameterSource (자동 매핑)
 *    - 자바빈 프로퍼티 규약(getXxx → xxx)을 보고 자동으로 매핑
 *    - getItemName() → key=itemName, value=실제 값
 *    SqlParameterSource param = new BeanPropertySqlParameterSource(item);
 *    template.update(sql, param, keyHolder);
 *
 *    ⚠️ 단점: 객체에 없는 값은 매핑 불가
 *    예) update() SQL에서는 :id 필요
 *        → ItemUpdateDto에 id 없음
 *        → 따라서 MapSqlParameterSource 사용해야 함
 *        
 * -----------------------------------------------------
 * [RowMapper - 조회 결과 매핑 방법]
 *
 * 1) 수동 매핑
 *    RowMapper<Item> mapper = (rs, rowNum) -> {
 *        Item item = new Item();
 *        item.setId(rs.getLong("id"));
 *        item.setItemName(rs.getString("item_name"));
 *        item.setPrice(rs.getInt("price"));
 *        item.setQuantity(rs.getInt("quantity"));
 *        return item;
 *    };
 *
 * 2) BeanPropertyRowMapper (자동 매핑)
 *    - 컬럼명 ↔ 자바빈 setter 자동 매핑
 *    - newInstance(Item.class) 사용
 *    - 내부적으로 리플렉션 활용
 *    ex) select id, price → setId(), setPrice() 자동 호출
 *
 * -----------------------------------------------------
 * [컬럼명 불일치 문제 해결]
 *
 * - DB: snake_case (item_name)
 * - Java: camelCase (itemName)
 *
 * 1) BeanPropertyRowMapper 자동 변환
 *    item_name → itemName 자동 변환 지원
 *
 * 2) 컬럼명이 전혀 다를 경우 → SQL 별칭(as) 사용
 *    ex) DB 컬럼: member_name, Java 필드: username
 *        select member_name as username from member
 *
 */

@Slf4j
public class JdbcTemplateItemRepositoryV2 implements ItemRepository {

	private final NamedParameterJdbcTemplate template;
	
	public JdbcTemplateItemRepositoryV2(DataSource dataSource) {
		this.template = new NamedParameterJdbcTemplate(dataSource);
	}
	
	/**
	 * RowMapper<Item>: ResultSet → Item 객체 변환기
	 * BeanPropertyRowMapper: DB 컬럼명과 객체 필드명을 자동 매핑
	 *  -> item_name → itemName (카멜 케이스 자동변환 지원)
	 */
	private RowMapper<Item> itemRowMapper() {
		return BeanPropertyRowMapper.newInstance(Item.class);
	}
	
	@Override
	public Item save(Item item) {
		String sql = "insert into item (item_name, price, quantity) "+
					 "values (:itemName, :price, :quantity)";
		
		/**
		 * BeanPropertySqlParameterSource(item) 
		 * 	-> item.getItemName(), getPrice(), getQuantity()를 자동으로 매핑.
		 * 
		 * KeyHolder
		 *  -> DB가 생성한 PK(id) 값을 받아옴
		 */
		SqlParameterSource param = new BeanPropertySqlParameterSource(item);
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		template.update(sql, param, keyHolder);
		
		long key = keyHolder.getKey().longValue();
		item.setId(key);
		return item;
	}

	@Override
	public Optional<Item> findById(Long id) {
		String sql = "select id, item_name, price, quantity from item where id = :id";
		
		try {
			/**
			 * - :id 파라미터를 Map.of("id", id)로 전달
			 * - queryForObject -> 결과가 반드시 1건일 때 사용.
			 */
			Map<String, Object> paramMap = Map.of("id", id);
			Item item = template.queryForObject(sql, paramMap, itemRowMapper());
			return Optional.of(item);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<Item> findAll(ItemSearchCond cond) {
		String itemName = cond.getItemName();
		Integer maxPrice = cond.getMaxPrice();

		/**
		 * BeanPropertySqlParameterSource(cond)
		 * 	-> cond.getItemName(), getMaxPrice()를 SQL의 :itemName, :maxPrice에 자동 바인딩.
		 */
		SqlParameterSource param = new BeanPropertySqlParameterSource(cond);
		
		String sql = "select id, item_name, price, quantity from item";
		// 동적 쿼리
		if (StringUtils.hasText(itemName) || maxPrice != null) {
			sql += " where";
		}

		boolean andFlag = false;
		if (StringUtils.hasText(itemName)) {
			sql += " item_name like concat('%',:itemName,'%')";
			andFlag = true;
		}

		if (maxPrice != null) {
			if (andFlag) {
				sql += " and";
			}
			sql += " price <= :maxPrice";
		}

		log.info("sql={}", sql);
		return template.query(sql, param, itemRowMapper());
	}

	@Override
	public void update(Long itemId, ItemUpdateDto updateParam) {
		String sql = "update item " + 
					 "set item_name=:itemName, price=:price, quantity=:quantity " + 
				     "where id=:id";
		
		SqlParameterSource paramMap = new MapSqlParameterSource()
					.addValue("itemName", updateParam.getItemName())
					.addValue("price", updateParam.getPrice())
					.addValue("quantity", updateParam.getQuantity())
					.addValue("id", itemId);
		
		template.update(sql, paramMap);
	}

	@Override
	public void delete(Long id) {
		String sql = "delete from item where id = :id";
		template.update(sql, Map.of("id", id));
	}

}
