package hello.springdb2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.memory.MemoryItemRepository;
import hello.springdb2.service.ItemService;
import hello.springdb2.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MemoryConfig {

    @Bean
	ItemRepository itemRepository() {
		return new MemoryItemRepository();
	}
	
    @Bean
	ItemService itemService() {
		return new ItemServiceV1(itemRepository());
	}
	
}
