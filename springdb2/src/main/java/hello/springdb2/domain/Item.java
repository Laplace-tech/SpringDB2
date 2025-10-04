package hello.springdb2.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * "Item 도메인" 엔티티 정리
 * 
 * 1. 역할(Domain 의미)
 * - 시스템에서 관리하는 상품(Item) 정보를 표현하는 도메인 객체
 * - 데이터베이스의 item "테이블과 매핑되는 JPA 엔티티(클래스)"
 * 
 * 2. 객체-테이블 매핑(ORM)
 * - JPA(Object Relational Mapping)는 "객체와 테이블을 매핑"하여
 *   개발자가 "객체 중심으로 데이터베이스 작업"을 가능하게 함
 * - @Entity 애너테이션을 붙이면 해당 클래스가 JPA 엔티티로 인식됨
 * 
 * 3. 생성자 규칙
 * - JPA 엔티티는 public 또는 protected 기본 생성자가 필수 : @NoArgsConstructor
 * - JPA가 리플렉션을 사용하여 객체 생성
 * - 필드 초기화를 위한 추가 생성자 가능
 * 
 * 4. JPA 적용 요약
 * - @Entity + @Id는 JPA 엔티티의 필수 구조
 * - @GeneratedValue 전략에 따라 PK 생성 방식 결정
 * - @Column은 선택 사항이며, 스프링 부트가 자동으로 매핑 처리 가능
 * --------------------------------------------------
 * [애너테이션 분석]
 * 
 * @Data 
 * - getter/setter, toString, equals/hashCode 자동 생성
 * - DTO, VO, Entity 클래스 작성 시 사용
 * - 자동 생성 메서드:
 *   @Getter, @Setter, @ToString, @EqualsAndHashCode, @RequiredArgsConstructor
 * 
 * @Entity (jakarta.persistence)
 * - JPA가 관리하는 엔티티 클래스 표시
 * - DB 테이블과 매핑됨
 * 
 * @Id 
 * - 엔티티의 PK 지정
 * 
 * @GeneratedValue(strategy = GenerationType.IDENTITY)
 * - PK 생성 전략 지정(DB 위임, MySQL AUTO_INCREMENT 방식)
 * 
 * @Column(name = "item_name", length = 10)
 * - 컬럼명 지정 및 제약조건 부여
 */
@Data
@Entity
@NoArgsConstructor
public class Item {

	/**
	 * - @Id: 엔티티의 식별자 지정
	 * - @GeneratedValue(strategy = GenerationType.IDENTITY):
	 *   DB에서 자동 증가(AUTO_INCREMENT) 전략 사용
	 *   INSERT 시점에 DB가 PK값을 생성
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	/**
	 * itemName
	 * - 상품명
	 * - @Column(name = "item_name", length = 10):
	 *   DB 컬럼명 "item_name"으로 매핑
	 *   최대 길이 10자로 제한
	 * - 생략 시 스프링 부트가 카멜케이스를 언터스코어로 자동 변환(itemName -> item_name)
	 *   
	 */
	@Column(name = "item_name", length = 10)
	private String itemName;
	private Integer price;
	private Integer quantity;
	
	public Item(String itemName, int price, int quantity) {
		this.itemName = itemName;
		this.price = price;
		this.quantity = quantity;
	}
}
