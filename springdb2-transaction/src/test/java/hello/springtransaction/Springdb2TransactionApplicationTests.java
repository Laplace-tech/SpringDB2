package hello.springtransaction;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class Springdb2TransactionApplicationTests {
	
	@Autowired
	private PlatformTransactionManager txManager;
	
	/**
	 * Creating new transaction with name [null]: PROPAGATION_REQUIRED,ISOLATION_DEFAULT
	 * Opened new EntityManager [SessionImpl(1944759838<open>)] for JPA transaction
	 * Exposing JPA transaction as JDBC [org.springframework.orm.jpa.vendor.HibernateJpaDialect$HibernateConnectionHandle@2ee5fe1e]
	 *
	 * Initiating transaction commit
	 * Committing JPA transaction on EntityManager [SessionImpl(1944759838<open>)]
	 * Closing JPA EntityManager [SessionImpl(1944759838<open>)] after transaction
	 */
	@Test
	void double_commit() {
	    log.info("트랜잭션1 시작");
	    TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
	    log.info("트랜잭션1 커밋");
	    txManager.commit(tx1);
		
	    log.info("트랜잭션2 시작");
	    TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
	    log.info("트랜잭션2 커밋");
	    txManager.commit(tx2);
	}
	
	@Test
	void inner_commit() {
	    log.info("외부 트랜잭션 시작");
	    TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
	    // outer.isNewTransaction()=true
	    log.info("outer.isNewTransaction()={}", outer.isNewTransaction());
	    
	    log.info("내부 트랜잭션 시작");
	    TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
	    // Participating in existing transaction, 
	    // inner.isNewTransaction()=false
	    log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
	    
	    log.info("내부 트랜잭션 커밋");
	    txManager.commit(inner);
	    
	    log.info("외부 트랜잭션 커밋");
	    txManager.commit(outer);
	}

	@Test
	void outer_rollback() {
	    log.info("외부 트랜잭션 시작");
	    TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
	    log.info("outer.isNewTransaction()={}", outer.isNewTransaction());
	    
	    log.info("내부 트랜잭션 시작");
	    TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
	    log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
	    
	    log.info("내부 트랜잭션 커밋");
	    txManager.commit(inner);
	    
	    log.info("외부 트랜잭션 롤백");
	    txManager.rollback(outer);
	}
	
	@Test
	void inner_rollback() {
	    log.info("외부 트랜잭션 시작");
	    TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
	    log.info("outer.isNewTransaction()={}", outer.isNewTransaction());
	 
	    log.info("내부 트랜잭션 시작");
	    TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
	    log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
	 
	    log.info("내부 트랜잭션 롤백");
	    txManager.rollback(inner);
	 
	    log.info("외부 트랜잭션 커밋 - 예외 발생");
	    Assertions.assertThatThrownBy(() -> txManager.commit(outer))
	         .isInstanceOf(UnexpectedRollbackException.class);
	}
	
	@Test
	void inner_rollback_requires_new() {
	    log.info("외부 트랜잭션 시작");
	    TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
	    log.info("outer.isNewTransaction()={}", outer.isNewTransaction());
	    
	    // Found thread-bound EntityManager [SessionImpl(1217159306<open>)] for JPA transaction
	    // Suspending current transaction, creating new transaction with name [null]
	    log.info("내부 트랜잭션 시작");
	    DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
	    definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	    TransactionStatus inner = txManager.getTransaction(definition);
	    log.info("inner.isNewTransaction()={}", inner.isNewTransaction());
	    
	    log.info("내부 트랜잭션 롤백");
	    txManager.rollback(inner); // 롤백
	    
	    log.info("외부 트랜잭션 커밋");
	    txManager.commit(outer); // 커밋
	}
}
