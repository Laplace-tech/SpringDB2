package hello.springdb2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.jpa.JpaItemRepositoryV3;
import hello.springdb2.service.ItemService;
import hello.springdb2.v2.ItemQueryRepositoryV2;
import hello.springdb2.v2.ItemRepositoryV2;
import hello.springdb2.v2.ItemServiceV2;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class V2Config {

	private final EntityManager entityManager;
	private final ItemRepositoryV2 itemRepositoryV2;
	
	@Bean
	ItemService itemService() {
		return new ItemServiceV2(itemRepositoryV2, itemQueryRepositoryV2());
	}
	
	@Bean
	ItemQueryRepositoryV2 itemQueryRepositoryV2() {
		return new ItemQueryRepositoryV2(entityManager);
	}
	
	@Bean
	ItemRepository itemRepository() {
		return new JpaItemRepositoryV3(entityManager);
	}
	
}
