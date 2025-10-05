package hello.springtransaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * [스프링 트랜잭션 이해]
 * 
 * 1. 트랜잭션 추상화 (Transaction Abstract)
 * - 데이터 접근 기술(JDBC, JPA, MyBatis 등)에 따라 트랜잭션 처리 방식이 다름
 * - 스프링은 PlatformTransactionManager 인터페이스를 통해 트랜잭션을 "추상화"하여,
 *   어떤 기술을 사용하든 동일한 코드로 트랜잭션을 제어할 수 있도록 함.
 *   
 *    ┌────────────────────────────────────────────┐
 *    │ PlatformTransactionManager (Interface)     │
 *    │  ├─ getTransaction() : 트랜잭션 시작(획득) │
 *    │  ├─ commit()        : 트랜잭션 커밋       │
 *    │  └─ rollback()      : 트랜잭션 롤백       │
 *    └────────────────────────────────────────────┘
 * 2. 스프링이 제공하는 트랜잭션 매니저 구현체
 * - DataSourceTransactionManager (JDBC, MyBatis)
 * - JpaTransactionManager (JPA)
 * - HibernateTransactionManager (Hibernate)
 * 
 *  ⚙️ 스프링 부트는 사용하는 기술에 맞는 트랜잭션 매니저를 자동으로 빈 등록함.
 *     → JDBC/MyBatis → DataSourceTransactionManager (or JdbcTransactionManager)
 *     → JPA → JpaTransactionManager
 *    
 * 3. 트랜잭션 적용 방식
 * 
 * (1) 선언적 트랜잭션 관리 
 * - @Transactional 애너테이션을 선언만 하면 스프링이 프록시를 통해 자동으로 트랜잭션을 적용 (실무 표준)
 * 
 * (2) 프로그래밍 방식 트랜잭션 관리
 * - TransactionTemplate 또는 PlatformTransactionManager를 직접 코드로 제어
 * - 비즈니스 로직에 트랜잭션 처리 코드가 섞이므로 잘 안 씀.
 * 
 * 4. 트랜잭션 동기화 매니저 (TransactionSynchronizationManager)
 * - 같은 트랜잭션을 유지하려면 동일한 DB 커넥션을 사용해야 한다
 * - 스프링은 내부적으로 트랜잭션 동기화 매니저를 이용하여 커넥션 등 리소스를 ThreadLocal 기반으로 공유/관리
 * 
 * 5. @Transactional (org.springframework.transaction.annotation.Transactional)
 *  - 선언만으로 트랜잭션 경계를 지정.
 *  - 메서드/클래스 단위로 선언 가능 (메서드 우선순위 > 클래스)
 *  - 주요 속성:
 *      - propagation: 트랜잭션 전파 방식 (기본값 = REQUIRED)
 *      - isolation: 트랜잭션 격리 수준
 *      - timeout: 제한 시간
 *      - readOnly: 읽기 전용 트랜잭션 여부 (성능 최적화)
 *      - rollbackFor / noRollbackFor: 롤백 대상 예외 지정
 * 
 */

@SpringBootApplication
public class Springdb2TransactionApplication {

	public static void main(String[] args) {
		SpringApplication.run(Springdb2TransactionApplication.class, args);
	}

}
