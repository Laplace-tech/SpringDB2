package hello.springtransaction.apply;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 스프링 AOP 기반 @Transactional의 자기 호출(Self-Invocation) 문제
 * 
 * 스프링의 @Transactional은 "프록시 객체"를 통해 트랜잭션을 시작하거나 커밋한다.
 * 따라서 프록시를 거치지 않는 직접적인 내부 메서드 호출에서는 트랜잭션이 적용되지 않는다.
 *
 * [1] InternalCallV1Test
 *     - 한 클래스 내부에서 `external()`이 같은 클래스의 `internal()`을 직접 호출.
 *     - 즉, 프록시를 거치지 않고 자기 자신(this)의 메서드를 부름.
 *     - 결과: @Transactional이 적용되지 않아 트랜잭션이 활성화되지 않음.
 *
 * [2] InternalCallV2Test
 *     - `CallService`가 다른 빈 `InternalService`를 주입받고,
 *       외부 클래스의 @Transactional 메서드(`internal()`)를 호출.
 *     - 이 호출은 프록시를 통해 일어나므로 트랜잭션이 정상 적용됨.
 *
 * 💡 결론
 *  - 자기 내부 호출(Self-Invocation)은 프록시를 통하지 않기 때문에 AOP가 동작하지 않는다.
 *  - 트랜잭션, @Async, @Cache 등 스프링 AOP 기반 기능 모두 동일한 제약이 있다.
 *
 */

@SpringBootTest
public class InternalCallV2Test {

	@Autowired
	CallService callService;
	
	@Test
	void externalCallV2() {
		callService.external();
	}
	
	@TestConfiguration
	static class InternalCallV2Config {
		@Bean
		CallService CallService() {
			return new CallService(internalService());
		}
		
		@Bean
		InternalService internalService() {
			return new InternalService();
		}
		
	}
	
	@Slf4j
	@RequiredArgsConstructor
	static class CallService {
		
		private final InternalService internalService;
		
		public void external() {
            log.info("call external");
            printTxInfo();
            System.out.println();
            internalService.internal();
		}
		
        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active={}", txActive);
        }
	}
	
	@Slf4j
	static class InternalService {
		
		@Transactional
		public void internal() {
			log.info("call Internal");
			printTxInfo();
		}
		
		private void printTxInfo() {
			boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
			log.info("tx active = {}", txActive);
		}
	}
	
}
