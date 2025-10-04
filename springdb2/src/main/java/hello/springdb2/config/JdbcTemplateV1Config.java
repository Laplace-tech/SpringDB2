package hello.springdb2.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import hello.springdb2.repository.ItemRepository;
import hello.springdb2.repository.jdbcTemplate.JdbcTemplateItemRepositoryV1;
import hello.springdb2.service.ItemService;
import hello.springdb2.service.ItemServiceV1;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class JdbcTemplateV1Config {

	private final DataSource dataSource;

	@Bean
	ItemRepository itemRepository() {
		return new JdbcTemplateItemRepositoryV1(dataSource);
	}
	
	@Bean
	ItemService itemService() {
		return new ItemServiceV1(itemRepository());
	}
}
