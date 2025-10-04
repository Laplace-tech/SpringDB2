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
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.util.StringUtils;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JdbcTemplateItemRepositoryV3 implements ItemRepository {
	
	private final NamedParameterJdbcTemplate template;
	private final SimpleJdbcInsert jdbcInsert;
	
	public JdbcTemplateItemRepositoryV3(DataSource dataSource) {
		this.template = new NamedParameterJdbcTemplate(dataSource);
		this.jdbcInsert = new SimpleJdbcInsert(dataSource)
				.withTableName("item") // 테이블 네임
				.usingGeneratedKeyColumns("id") // PK 컬럼지정
				.usingColumns("item_name", "price", "quantity");
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
		
		/**
		 * BeanPropertySqlParameterSource(item) 
		 * 	-> item.getItemName(), getPrice(), getQuantity()를 자동으로 매핑.
		 * 
		 * KeyHolder
		 *  -> DB가 생성한 PK(id) 값을 받아옴
		 */
		SqlParameterSource param = new BeanPropertySqlParameterSource(item);
		Number key = jdbcInsert.executeAndReturnKey(param);
		item.setId(key.longValue());
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

	public NamedParameterJdbcTemplate getJdbcTemplate() {
		return template;
	}

	@Override
	public void delete(Long id) {
		String sql = "delete from item where id = :id";
		template.update(sql, Map.of("id", id));
	}
	
}
