package hello.springdb2.repository;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ItemUpdateDto (상품 수정용 DTO)
 * 
 * - 상품 수정 요청 시 전달할 데이터를 담는 객체
 * - 불필요한 필드(id 등)는 제외하고, 수정 가능한 필드만 포함
 * - 서비스 계층 -> 리포지토리 계층으로 데이터 전달에 사용
 */

@Data
@NoArgsConstructor
public class ItemUpdateDto {

	private String itemName;
	private Integer price;
	private Integer quantity;
	
	public ItemUpdateDto(String ItemName, int price, int quantity) {
		this.itemName = ItemName;
		this.price = price;
		this.quantity = quantity;
	}
}
