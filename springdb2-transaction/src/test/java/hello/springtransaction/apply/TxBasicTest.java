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
