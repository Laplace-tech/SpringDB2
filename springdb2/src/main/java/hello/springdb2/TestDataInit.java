package hello.springdb2;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import hello.springdb2.domain.Item;
import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.ItemUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TestDataInit {

    private final ItemRepository itemRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        log.info("=== TestDataInit 시작 ===");

        // 저장 (Create)
        itemRepository.save(new Item("itemX", 10000, 10));
        itemRepository.save(new Item("itemY", 20000, 20));
        itemRepository.save(new Item("itemZ", 30000, 30));

        // 단건 조회 (Read)
        itemRepository.findById(1L)
                .ifPresentOrElse(
                        item -> log.info("조회 성공: {}", item),
                        () -> log.warn("조회 실패: id=1")
                );

        // 수정 (Update)
        itemRepository.update(3L, new ItemUpdateDto("itemA", 123, 456));
        
        // 삭제 (Delete)
        itemRepository.delete(3L);
        log.info("=== TestDataInit 종료 ===");
    }
}
