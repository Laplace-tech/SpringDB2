package hello.springdb2.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.jpa.JpaItemRepositoryV3;
import hello.springdb2.service.ItemService;
import hello.springdb2.service.ItemServiceV1;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class QuerydslConfig {

	private final EntityManager entityManager;
	
	@Bean
	ItemService itemService() {
		return new ItemServiceV1(itemRepository());
	}
	
	@Bean
	ItemRepository itemRepository() {
		return new JpaItemRepositoryV3(entityManager);
	}
	
}
