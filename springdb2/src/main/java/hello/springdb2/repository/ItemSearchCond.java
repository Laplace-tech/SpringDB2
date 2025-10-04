package hello.springdb2.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ItemSearchCond (검색 조건 DTO)
 * 
 * - 상품 검색 시 조건을 담는 데이터 전송 객체(DTO)
 * - "검색 조건"을 하나의 객체로 전달하여 메서드 파라미터 단순화
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemSearchCond {

	private String itemName;
	private Integer maxPrice;
	
}
