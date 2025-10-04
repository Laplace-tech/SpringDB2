package hello.springdb2.repository.jdbcTemplate;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.StringUtils;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;
import lombok.extern.slf4j.Slf4j;


/**
 * [조회]
 * 
 * - 단건 조회 (숫자 조회)
 * int rowCount = jdbcTemplate.queryForObject("select count(*) from t_actor", Integer.class);
 * 
 * - 단건 조회 (숫자 조회, 파라미터 바인딩)
 * int countOfActorsNamedJoe = jdbcTemplate.queryForObject(
 * 			"select count(*) from t_actor where first_name = ?",
 * 	 		Integer.class, 
 * 	    	"Joe");
 * 
 * - 단건 조회 (문자 조회, 파라미터 바인딩)
 * String lastName = jdbcTemplate.queryForObject(
 * 			"select last_name from t_actor where id = ?",
 * 			String.class,
 * 			111L);
 * 
 * - 단건 조회 (객체 조회, 파라미터 바인딩)
 * Actor actor = jdbcTemplate.queryForObject(
 * 			"select first_name, last_name from t_actor where id = ?",
 * 			(rs, rowNum) -> {
 * 				Actor newActor = new Actor();
 * 				newActor.setFirstName(rs.getString("first_name"));
 * 				newActor.setLastName(rs.getString("last_name"));
 * 				return newActor;
 * 			}, 
 * 			111L);
 * 
 * - 목록 조회 (객체 조회)
 * List<Actor> actorList = jdbcTemplate.query(
 * 			"select first_name, last_name from t_actor",
 * 			(rs, rowNum) -> {
 * 				Actor actor = new Actor();
 * 				actor.setFirstName(rs.getString("first_name"));
 * 				actor.setLastName(rs.getString("last_name"));
 * 				return actor;
 * 			});
 *----------------------------------------------------------------------- 
 * [변경] 
 * 
 * - INSERT
 * jdbcTemplate.update(
 * 		"insert into t_actor (first_name, last_name) values (?, ?),
 * 		"Anna Viktorovna", "Choe");
 * 
 * - UPDATE
 * jdbcTemplate.update(
 * 		"update t_actor set last_name = ? where id = ?",
 * 		"Choe", 1111L);
 * 
 * - DELETE
 * jdbcTemplate.update(
 * 		"delete from t_actor where id = ?",
 * 		Long.valueOf(actorId));
 */

/**
 * JdbcTemplateItemRepositoryV1
 * - ItemRepository 인터페이스 구현체
 * - Spring JDBC의 "JdbcTemplate"을 사용해서 DB CRUD 구현
 * - JdbcTemplate은 JDBC "보일러 플레이트 코드"(try/catch, close, preparedStatement 등)를 줄여줌
 */

@Slf4j
public class JdbcTemplateItemRepositoryV1 implements ItemRepository {
	
	private final JdbcTemplate template;
	
	public JdbcTemplateItemRepositoryV1(DataSource dataSource) {
		this.template = new JdbcTemplate(dataSource);
	}
	
	private final RowMapper<Item> itemRowMapper = (rs, rowNum) -> {
		Item item = new Item();
		item.setId(rs.getLong("id"));
		item.setItemName(rs.getString("item_name"));
		item.setPrice(rs.getInt("price"));
		item.setQuantity(rs.getInt("quantity"));
		return item;
	};
	
	@Override
	public Item save(Item item) {
		String sql = "insert into item (item_name, price, quantity) values (?, ?, ?)";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		template.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
			ps.setString(1, item.getItemName());
			ps.setInt(2, item.getPrice());
			ps.setInt(3, item.getQuantity());
			return ps;
		}, keyHolder);
		
		long key = keyHolder.getKey().longValue();
		item.setId(key);
		return item;
	}

	@Override
	public Optional<Item> findById(Long id) {
		String sql = "select id, item_name, price, quantity from item where id = ?";
		try {
			Item item = template.queryForObject(sql, itemRowMapper, id);
			return Optional.of(item);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<Item> findAll(ItemSearchCond cond) {
		String itemName = cond.getItemName();
		Integer maxPrice = cond.getMaxPrice();

		String sql = "select id, item_name, price, quantity from item";
		// 동적 쿼리
		if (StringUtils.hasText(itemName) || maxPrice != null) {
			sql += " where";
		}

		boolean andFlag = false;
		List<Object> param = new ArrayList<>();
		if (StringUtils.hasText(itemName)) {
			sql += " item_name like concat('%',?,'%')";
			param.add(itemName);
			andFlag = true;
		}

		if (maxPrice != null) {
			if (andFlag) {
				sql += " and";
			}
			sql += " price <= ?";
			param.add(maxPrice);
		}

		log.info("sql={}", sql);
		return template.query(sql, itemRowMapper, param.toArray());
	}

	@Override
	public void update(Long itemId, ItemUpdateDto updateParam) {
		String sql = "update item set item_name=?, price=?, quantity=? where id=?";
		template.update(sql, 
				updateParam.getItemName(),
				updateParam.getPrice(),
				updateParam.getQuantity(),
				itemId);
	}

	@Override
	public void delete(Long id) {
		String sql = "delete from item where id = ?";
		template.update(sql, id);
	}


}
