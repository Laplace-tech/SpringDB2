package hello.springtransaction.apply;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * [스프링 빈 초기화 시점]
 * 
 * 트랜잭션 AOP는 프록시 객체를 기반으로 동작한다.
 * 그런데 @PostConstruct는 스프링 빈이 생성된 "직후" 실행되므로
 * 아직 프록시 객체가 적용되기 전이다. 따라서 @Transactional이 무시된다.
 *
 * 반면, @EventListener(ApplicationReadyEvent.class)는 
 * 스프링 컨테이너 초기화가 완전히 끝난 이후(프록시 적용 이후)에 실행된다
 * 이 시점에서는 트랜잭션 AOP가 활성화되어 있으므로 @Transacional이 정상 적용된다.
 * 
 */

@SpringBootTest
public class InitTxTest {
	
    @Autowired
    Hello hello;
  
    @Test
    void go() {
        //초기화 코드는 스프링이 초기화 시점에 호출한다.
    }
  
    @TestConfiguration
    static class InitTxTestConfig {
        @Bean
        Hello hello() {
            return new Hello();
        }
    }
	
	@Slf4j
	static class Hello {
		
		@PostConstruct
		@Transactional
		public void initV1() {
			boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init @PostConstruct tx active={}", isActive);
		}
		
		@Transactional
		@EventListener(value = ApplicationReadyEvent.class)
		public void initV2() {
			boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init ApplicationReadyEvent tx active={}", isActive);
		}
		
	}
	
}
