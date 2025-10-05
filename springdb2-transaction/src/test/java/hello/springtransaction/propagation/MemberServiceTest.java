package hello.springtransaction.propagation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import lombok.extern.slf4j.Slf4j;

/**
 * 스프링 트랜잭션 + AOP 프록시 동작
 * 
 * 1. 애플리케이션 구동 시점 (Spring Context 생성)
 * - @Transactional 애너테이션이 붙은 @Bean에 대해 스프링이 프록시 생성
 * - 스프링 AOP(Proxy-based Transaction Management)를 사용
 * - 이 프록시는 실제 서비스 메서드 호출 전/후에 트랜잭션 시작/종료 로직 삽입
 * 
 * 2. 메서드 호출 시점 (AOP 프록시 진입)
 * - 클라이언트가 service.someMethod()를 호출
 * - 프록시 객체가 이 요청을 가로채어 먼저 진입
 * - 트랜잭션 매니저에게 트랜잭션 시작 요청
 * 
 *  : TransactionInterceptor.invoke()
 *      ↓
 *    TransactionAspectSupport.invokeWithinTransaction()
 *    
 * 3. 트랜잭션 시작 과정 (TransactionManager.doBegin)
 * - DataSourceTransactionManager 사용 시:
 *   
 *   a) 커넥션 휙득:
 *      Connection con = DataSourceUtils.getConnection(dataSource); 트랜잭션 동기화
 *      
 *   b) autoCommit(false) 설정:
 *      con.setAutoCommit(false);
 *
 *   c) ConnectionHolder 생성:
 *      ConnectionHolder holder = new ConnectionHolder(con);
 *
 *   d) 트랜잭션 동기화 매니저에 리소스 바인딩:
 *      TransactionSynchronizationManager.bindResource(dataSource, holder);
 *      
 *  -> 이렇게 하면 같은 스레드 내에서 해당 데이터소스의 모든 JDBC 접근이 동일 커넥션을 사용
 * 
 * 4. 비즈니스 로직 실행 (트래잭션 범위 안)
 * - JdbcTemplate, MyBatis, JPA 등이 TransactionSynchronizationManager에서
 *   현재 바인딩된 커넥션을 조회해서 사용
 *   
 *  JdbcTemplate 내부 예시:
 *    Connection con = DataSourceUtils.getConnection(dataSource);
 *    // 이미 바인딩된 Connection 반환, SQL 실행
 *    
 * 5. 트랜잭션 종료 시점 (AOP 프록시 종료)
 *    - 프록시가 비즈니스 메소드 실행 후 제어권 회수.
 *    - 성공 시: commit, 예외 발생 시: rollback 수행
 *    
 * 6. 트랜잭션 동기화 해제
 *    - 트랜잭션 종료 후 ConnectionHolder 언바인딩.
 *    - Connection 반환 또는 커넥션 풀에 반환.
 *    
 * ----------------------------------------------
 * 핵심 포인트
 * ----------------------------------------------
 * - @Transactional 은 스프링 AOP 기반 "Proxy"를 통해 동작.
 * - 트랜잭션 매니저는 트랜잭션 시작 시 "Connection"을 "ThreadLocal"에 바인딩
 * - 데이터 접근 기술들은 TransactionSynchronizationManager를 통해 "같은 Connection 재사용"
 * - 트랜잭션 종료 시 commit/rollback + "리소스 정리" 수행.
 */
@Slf4j
@SpringBootTest
public class MemberServiceTest {

	@Autowired
	MemberService memberService;
	@Autowired
	MemberRepository memberRepository;
	@Autowired
	LogRepository logRepository;
	
	/**
	* MemberService @Transactional:ON
	* MemberRepository @Transactional:ON
	* LogRepository @Transactional(REQUIRES_NEW) Exception
	*/
	@Test
	void recoverException_success() {
	    //given
	    String username = "로그예외_recoverException_success";
	  
	    //when
	    memberService.joinV2(username);
	  
	    //then: member 저장, log 롤백
	    assertTrue(memberRepository.find(username).isPresent());
	    assertTrue(logRepository.find(username).isEmpty());
	}
	
	/**
	* MemberService @Transactional:ON
	* MemberRepository @Transactional:ON
	* LogRepository @Transactional:ON Exception
	*/
	@Test
	void recoverException_fail() {
	    //given
	    String username = "로그예외_recoverException_fail";
	  
	    //when
	    assertThatThrownBy(() -> memberService.joinV2(username))
	        .isInstanceOf(UnexpectedRollbackException.class);
	  
	    //then: 모든 데이터가 롤백된다.
	    assertTrue(memberRepository.find(username).isEmpty());
	    assertTrue(logRepository.find(username).isEmpty());
	}
	
	/**
	* MemberService @Transactional:ON
	* MemberRepository @Transactional:ON
	* LogRepository @Transactional:ON Exception
	*/
	@Test
	void outerTxOn_fail() {
	    //given
	    String username = "로그예외_outerTxOn_fail";
	  
	    //when
	    assertThatThrownBy(() -> memberService.joinV1(username))
	    .isInstanceOf(RuntimeException.class);
	  
	    //then: 모든 데이터가 롤백된다.
	    assertTrue(memberRepository.find(username).isEmpty());
	    assertTrue(logRepository.find(username).isEmpty());
	}
	
	/**
	* MemberService @Transactional:ON
	* MemberRepository @Transactional:ON
	* LogRepository @Transactional:ON
	*/
	@Test
	void outerTxOn_success() {
	    //given
	    String username = "outerTxOn_success";
	  
	    //when
	    memberService.joinV1(username);
	  
	    //then: 모든 데이터가 정상 저장된다.
	    assertTrue(memberRepository.find(username).isPresent());
	    assertTrue(logRepository.find(username).isPresent());
	}
	
    /**
    * MemberService @Transactional:ON
    * MemberRepository @Transactional:OFF
    * LogRepository @Transactional:OFF
    */
    @Test
    void singleTxFail() {
        //given
        String username = "로그예외singleTx";
      
        //when
        assertThatThrownBy(() -> memberService.joinV1(username))
            .isInstanceOf(RuntimeException.class);
        
        assertTrue(memberRepository.find(username).isEmpty()); // 롤백
        assertTrue(logRepository.find(username).isEmpty()); // 롤백
    }
	
    /**
    * MemberService @Transactional:ON
    * MemberRepository @Transactional:OFF
    * LogRepository @Transactional:OFF
    */
    @Test
    void singleTx() {
        //given
        String username = "singleTx";
      
        //when
        memberService.joinV1(username);
      
        //then: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent()); // 커밋
        assertTrue(logRepository.find(username).isPresent()); // 커밋
    }
	
    /**
    * MemberService @Transactional:OFF
    * MemberRepository @Transactional:ON
    * LogRepository @Transactional:ON
    */
    @Test
    void outerTxOff_success() {
        //given
        String username = "outerTxOff_success";
        //when
        memberService.joinV1(username);
        //then: 모든 데이터가 정상 저장된다.
        assertTrue(memberRepository.find(username).isPresent()); // 커밋
        assertTrue(logRepository.find(username).isPresent()); // 커밋
    }

    /**
    * MemberService @Transactional:OFF
    * MemberRepository @Transactional:ON
    * LogRepository @Transactional:ON
    */
    @Test
    void outerTxOff_fail() {
        //given
        String username = "로그예외_outerTxOff_fail";
      
        //when
        assertThatThrownBy(() -> memberService.joinV1(username))
            .isInstanceOf(RuntimeException.class);
      
        //then: 완전히 롤백되지 않고, member 데이터가 남아서 저장된다. : 데이터 무결성 파괴
        assertTrue(memberRepository.find(username).isPresent()); // 커밋
        assertTrue(logRepository.find(username).isEmpty()); // 롤백
    }
    
}
