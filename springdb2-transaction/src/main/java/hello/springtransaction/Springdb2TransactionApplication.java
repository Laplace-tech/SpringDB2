package hello.springtransaction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


/**
 * [스프링 트랜잭션]
 * 
 * - 트랜잭션(Transaction) : 데이터베이스에서 하나의 작업 단위를 의미
 * - 트랜잭션의 특징: ACID(Atomicity, Consistency, Isolation, Durability)
 * - 여러 데이터 조작 작업을 하나의 단위로 묶어, 성공 시 전부 반영(커밋), 실패 시 전부 취소(롤백) 가능
 * 
 * 데이터 접근 기술(JDBC, JPA, MyBatis 등)에 따라 트랜잭션 코드 작성 방식이 다름
 * - JDBC 방식: Connection 객체를 직접 관리하면서 setAutoCommit(false), commit(), rollback() 사용.
 * - JPA 방식: EntityManager, EntityTransaction을 사용해서 트랜잭션 관리.
 * 
 * [스프링 트랜잭션 추상화]
 * 
 * - 스프링은 트랜잭션 처리를 위한 "추상화 계층" 제공
 * - PlatformTransactionManager 인터페이스를 통해 트랜잭션 처리 로직을 통일
 * 
 * PlatformTransactionManager 주요 메서드
 * - getTransaction(...) : 트랜잭션 시작
 * - commit(status) : 커밋
 * - rollback(status) : 롤백
 * 
 * 스프링 부트는 자동으로 데이터 접근 기술(JDBC, JPA 등)을 인식하고
 * 적절한 트랜잭션 매니저르 스프링 빈으로 등록한다
 * - JdbcTemplate/MyBatis -> DataSourceTransactionManager
 * - JPA -> JpaTransactionManager
 * 
 * [트랙잭션 관리 방식]
 * 
 * (1) 선언적 트랜잭션 관리(Declarative Transaction Management)
 * - @Transactional 애너테이션 사용 (대부분 이거 씀)
 * - AOP 기반으로 트랜잭션 처리 로직 자동 적용
 * - 코드 간결, 유지보수 용이
 * 
 * (2) 프로그래밍 방식 트랜잭션 관리(Programmatic Transaction Management)
 * - 트랜잭션 매니저 직접 사용, 개발자가 커밋/롤백 제어
 * - 코드 복잡도, 기술 결합도 증가
 * 
 * [스프링 트랜잭션과 AOP 동작 과정]
 * 
 * - @Transactional 애너테이션을 사용 -> 스프링이 자동으로 "트랜잭션 프록시" 생성
 * - 프록시가 서비스 메서드 호출 전후에 트랜잭션 로직 처리 (자동 커밋 OFF, 커밋 or 롤백)
 * - 동작 과정:
 *   (a): 클라이언트(Service 호출자) -> 트랜잭션 프록시 호출
 *   (b): 트랜잭션 프록시:
 *        - PlatformTransactionManager.getTransaction(...) 호출 -> 트랜잭션 시작
 *        - 실제 서비스 메서드 호출
 *        - 메서드 정상 종료 시 "커밋"
 *        - 예외 발생 시 "롤백"
 * 
 */

@SpringBootApplication
public class Springdb2TransactionApplication {

	public static void main(String[] args) {
		SpringApplication.run(Springdb2TransactionApplication.class, args);
	}

}
