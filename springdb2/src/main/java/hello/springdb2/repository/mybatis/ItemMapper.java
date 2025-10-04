package hello.springdb2.repository.mybatis;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemSearchCond;
import hello.springdb2.repository.ItemUpdateDto;

/**
 * 1. [동적 SQL 예시]
 * 
 *   - if
 *     <select id="findActiveBlogWithTitleLike" resultType="Blog">
 *      	SELECT * FROM BLOG
 *      	WHERE state = 'ACTIVE'
 *      	<if test="title != null">
 *      		AND title like #{title}
 *      	</if>
 *     </select>
 *     
 *   - choose / when / otherwise
 *     <select id="findActiveBlogLike" resultType="Blog">
 *     		SELECT * FROM BLOG WHERE state = 'ACTIVE'
 *     		<choose>
 *     			<when test="title != null">
 *     				AND title like #{title}
 *     			</when>
 *     			<when test="author != null and author.name != null">
 *     				AND author_name like #{author.name}
 *     			</when>
 *     			<otherwise>
 *     				AND feature = 1
 *     			</otherwise>
 *     		</choose>
 *     </select>
 *     
 *   - trim / where / set
 *     <select id="findActiveBlogLike" resultType="Blog">
 *     		SELECT * FROM BLOG
 *     		<where>
 *     			<if test="state != null">
 *     				state = #{state}
 *     			</if>
 *     			<if test="title != null">
 *     				AND title LIKE #{title}
 *     			</if>
 *     			<if test="author != null and author.name != null">
 *     				AND author_name LIKE #{author.name}
 *     			</if>
 *     		</where>
 *     </select>
 *     
 *     <select id="findBlogByConditions" resultType="Blog">
 *         SELECT * FROM BLOG
 *         <trim prefix="WHERE" prefixOverrides="AND |OR ">
 *             <if test="state != null">
 *                 AND state = #{state}
 *             </if>
 *             <if test="title != null">
 *                 AND title LIKE = #{title}
 *             </if>
 *         </trim>
 *     </select>
 *     
 * --------------------------------------------------------------------------
 * 2. MyBatis 동작 원리
 * 
 * 1) @Mapper
 * - MyBatis에서는 @Mapper를 사용해서 SQL을 실행한다
 * - @Mapper 애너테이션은 MyBatis-Spring에게 "이 인터페이스는 @Mapper다" 라고 알려준다
 * - 애플리케이션이 실행될 때, MyBatis-Spring이 지정된 패키지를 검색해서 @Mapper가 붙은 인터페이스를 찾는다
 * 
 * 2) @Mapper 인터페이스에 대한 동적 프록시 생성
 * - "프록시(Proxy)"는 실제 구현 객체 대신 동작하는 대리 객체이다
 * - MyBatis는 java.lang.reflect.Proxy를 사용해서 런타임에 자동으로 Mapper 인터페이스의 프록시 객체를 만든다
 * - Mapper 인터페이스에는 직접 구현체가 없지만, 프록시가 SQL 실행 역할을 대신한다.
 * - 즉, MyBatis가 자동으로 구현체 역할을 하는 프록시 객체를 만들어서 제공한다
 * 
 * 3) Mapper XML과 메서드 매핑
 * - Mapper 인터페이스의 메서드 이름과 MapperXML의 <select>, <insert>, <update> 등의 "id"가 매핑된다.
 *   <select id="findAll" resultType="Item">
 *      SELECT * FROM item
 *   </select>
 *   
 * 4) 메서드 호출 시 동작
 * - 개발자가 itemMapper.findAll(cond)를 호출하면:
 *   1. 프록시 객체가 호출을 가로챈다
 *   2. 해당 메서드 이름(findAll)을 키로 MapperXML에서 SQL 문장을 찾는다
 *   3. 파라미터 객체(cond)의 값들을 #{} 위치에 바인딩 한다
 *   4. 동적 SQL 태그를 해석해서 최종 SQL을 만든다.
 * 
 * 5) SQL 실행
 * - MyBatis는 내부적으로 SqlSession을 사용한다
 * - SqlSession은 JDBC의 PreparedStatement를 생성한다.
 * - 파라미터 바인딩(#{} 부분 채우기) -> DB에 SQL 실행 요청
 * - 실행이 끝나면 결과를 받아온다.
 * 
 * 6) 결과 매핑(Result Mapping)
 * - MyBatis는 DB 결과(ResultSet)를 자바 객체로 변환한다
 * - 기본 규칙: 컬럼명 -> 자바 필드명으로 매핑
 *   예: DB의 item_name -> 자바의 itemName (map-underscore-to-camel-case=true 옵션이 켜져야 함)
 * 
 * 7) 반환
 * - MyBatis는 Mapper 메서드의 반환 타입에 맞춰 결과를 반환한다  
 * 
 * 8) 스프링 빈 등록
 * - MyBatis가 만든 프록시 객체를 스프링 컨테이너에 "Bean"으로 등록한다.
 * - 개발자는 그냥 @Autowired로 Mapper 인터페이스를 주입받아 사용할 수 있다  
 */

@Mapper
public interface ItemMapper {

//	@Insert("INSERT INTO item (item_name, price, quantity) VALUES (#{itemName}, #{price}, #{quantity})")
	void save(Item item);
	
	void update(@Param("id") Long id, @Param("updateParam") ItemUpdateDto updateParam);
	
//	@Select("SELECT id, item_name, price, quantity FROM item WHERE id = #{id}")
	Optional<Item> findById(Long id);
	
	List<Item> findAll(ItemSearchCond itemSearch);
	
	@Delete("DELETE FROM item WHERE id = #{id}")
	void delete(Long id);
	
}
