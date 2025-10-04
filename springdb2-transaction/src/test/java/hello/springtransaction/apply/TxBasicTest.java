package hello.springtransaction.apply;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * 1. 선언적 트랜잭션과 AOP 기반 동작
 * -----------------------------------
 * - @Transactional 애노테이션이 클래스나 메서드에 적용되면,
 *   스프링은 AOP(Aspect-Oriented Programming)를 이용해 '트랜잭션 프록시'를 생성함.
 * - 트랜잭션 프록시: 실제 객체 대신 스프링 컨테이너에 등록되고 주입됨.
 *   → 실제 객체 호출 전후로 트랜잭션 시작(commit/rollback) 로직이 실행됨.
 *
 * -----------------------------------
 * - AopUtils.isAopProxy(bean)로 확인 가능.
 * - 실행 결과 예:
 *     aop class=class com.example.TxBasicTest$BasicService$$EnhancerBySpringCGLIB$$xxxxxx
 *   → 클래스명이 `$$EnhancerBySpringCGLIB`로 끝나면 CGLIB 기반 프록시 적용.
 * - 의미: 실제 BasicService 객체 대신 CGLIB이 생성한 프록시가 스프링 빈으로 등록됨.
 *
 * 3. 프록시 동작 방식
 * -----------------------------------
 * 클라이언트 → basicService.tx() 호출
 *     ↓
 * 트랜잭션 프록시(BasicService$$CGLIB)
 *     ↓ (트랜잭션 시작)
 * 실제 BasicService.tx() 실행
 *     ↓ (트랜잭션 커밋/롤백)
 * 프록시 반환
 *
 * - nonTx() 호출 시 @Transactional이 없으므로 트랜잭션 프록시가 "트랜잭션 생성 안 함".
 *
 */


@Slf4j
@SpringBootTest
public class TxBasicTest {

    @Autowired
    BasicService basicService;

    @Test
    void proxyCheck() {
    	log.info("aop class = {}", basicService.getClass());
    	assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }
    
    @Test
    void txTest() {
    	basicService.tx();
    	basicService.nonTx();
    }
    
    @TestConfiguration
	static class TxApplyBasicConfig {

		@Bean
		BasicService basicService() {
			return new BasicService();
		}

	}

	@Slf4j
	static class BasicService {

		@Transactional
		public void tx() {
			log.info("call transaction");
			boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
			log.info("tx active = {}", txActive);
		}

		public void nonTx() {
			log.info("call nonTx");
			boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
			log.info("tx active={}", txActive);
		}

	}

}
