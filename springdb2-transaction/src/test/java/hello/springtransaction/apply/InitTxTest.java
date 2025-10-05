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
		
		/**
		 * 1. 스프링은 @Bean을 생성할 때 원본 객체를 만든 뒤 @Transactional이 붙어 있으면 
		 *   프록시 객체로 감싸서 트랜잭션 기능을 추가한다.
		 * 2. 그런데 @PostConstruct는 @Bean 초기화 직후 (프록시 적용 전) 호출된다.
		 * 
		 * 즉, @PostConstruct 실행 시점은 "프록시 AOP가 아직 적용되지 않은 상태"
		 * 그래서, @Transactional은 완전히 무시된다. (근데 어떤 빡통이 이 두 애너테이션을 같이 쓰려할까?)
		 */
		@PostConstruct
		@Transactional
		public void initV1() {
			boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init @PostConstruct tx active={}", isActive);
		}
		
		/**
		 * 스프링 컨테이너가 모든 빈 생성과 프록시 적용까지 완료된 후 실행되도록 하려면
		 * 아래처럼 @EventListner(ApplicationReadyEvent.class)를 사용한다.
		 * 
		 * 이 시점에서는..
		 * - 스프링 AOP 프록시가 완전히 적용된 이후
		 * - 트랜잭션 프록시를 통해 호출됨
		 * - 따라서 transaction active = true;
		 */
		@Transactional
		@EventListener(value = ApplicationReadyEvent.class)
		public void initV2() {
			boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init ApplicationReadyEvent tx active={}", isActive);
		}
		
	}
	
}
