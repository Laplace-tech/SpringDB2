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


@Slf4j
@SpringBootTest
public class TxBasicTest {

    @Autowired
    BasicService basicService;

    /**
     * 클래스 이름이 "EnhancerBySpringCGLIB"이 포함되어 있으면 프록시 객체
     * - 스프링 컨테이너에 등록된 실제 빈은 BasicService의 진짜 인스턴스가 아니라
     *   CGLIB이 생성한 "프록시 객체"이다
     * - 이 프록시는 트랜잭션 시작/커밋/롤백을 대신 처리함
     */
    @Test
    void proxyCheck() {
    	log.info("aop class = {}", basicService.getClass());
    	assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }
    
    /**
     * @Transactional이 붙은 BasicService.tx() 메서드가 호출됨
     * - 스프링 AOP가 이 메서드를 감싸서 트랜잭션을 시작
     * - 기본 전파 옵션: PROPAGATION_REQUIRED -> 기존 트랜잭션이 없으면 새로 생성
     * - 새로운 물리 트랜잭션 시작
     * - 하이버네이트의 EntityManager가 생성되어 내부적으로 DB 커넥션을 가져옴 
     * - BasicService.tx() 메서드가 정상 종료 -> 예외 없음 -> 커밋 진행
     * - 스프링이 JpaTransactionManager를 통해 실제 DB 커밋 수행
     * - 하이버네이트 -> JDBC -> DB 커밋 호출
     * - 이후 EntityManager 닫음(close())
     */
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

		/**
		 * 현재 스레드에 트랜잭션이 활성화되어 있는지를 확인
		 */
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
