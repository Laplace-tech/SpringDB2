package hello.springdb2;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import hello.springdb2.config.V2Config;
import hello.springdb2.repository.ItemRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * 데이터 접근 기술(SQL 기반)
 * 
 * ----------------------------------
 * SQL Mapper 계열 (개발자가 직접 SQL 작성)
 * ----------------------------------
 * 
 * 1. JdbcTemplate (Spring 제공)
 *  - JDBC의 반복 작업(커넥션 휙득, PreparedStatement, ResultSet 정리)을 제거해주는 헬퍼 클래스
 *  - 장점: 단순한 CRUD 구현 편리, 예외를 Spring 공통 예외(DataAccessException)로 변한
 *  - 단점: SQL과 매핑을 직접 작성해야 함
 *  
 * 2. MyBatis
 *  - SQL을 XML이나 애너테이션으로 작성 -> 결과를 객체로 자동 매핑
 *  - 장점: 복잡한 SQL, 동적 SQL 지원
 *  - 단점: SQL은 여전히 직접 관리해야 함
 *  
 * ------------------------------
 * ORM 계열 (객체 중심, SQL을 자동 생성)
 * ------------------------------
 * 
 * 1. JPA (Java Persistence API)
 *  - 자바 표준 ORM 스펙, 엔티티(@Entity)와 테이블을 매핑
 *  - 장점: 반복되는 CRUD 자동화, 객체 그래프 관리, DB 종속성 줄임
 *  - 단점: 복잡한 쿼리는 직접 작성 필요(JPQL, 네이티브 SQL, QueryDSL)
 *  
 * 2. Hibernate
 *  - JPA의 대표 구현체
 *  - 캐시, 성능 최적화, 확장 기능 제공
 *  
 * 3. Spring Data JPA
 *  - JPA 위에 만들어진 생산성 도구
 *  - JpaRepository 인터페이스 상속만 해도 CRUD, 페이징 기능 제공
 *  - 메소드 이름만으로 쿼리 생성 (ex: findByMoneyGreaterThan)
 *  
 * 4. Querydsl
 *  - 타입 안전한 쿼리 작성 가능
 *  - 동적 조건을 깔끔하게 처리
 * 
 */

/**
 * 1. 애플리케이션 시작 단계
 * 
 * 1.1 Main Method 실행
 *  - Springdb2Application.main() 메서드가 실행된다
 *  - Spring Boot 애플리케이션을 실행하는 핵심 엔트리 포인트
 *  - 여기서 스프링 부트는 애플리케이션 컨텍스트(ApplicationContext)를 생성한다.
 *  
 * 1.2 애플리케이션 컨텍스트 생성
 *  - SpringBootApplication 애너테이션이 붙은 클래스가 *컴포넌트 스캔(Component Scan)*을 시작한다
 *  - scanBasePackages = "hello.springdb2.web" 설정에 의해 hello.springdb2.web 패키지와
 *    하위 패키지만 스캔된다.
 *  - 다른 패키지는 스캔되지 않으므로 @Import(MemoryConfig.class)를 통해 명시적으로 등록된다
 *  
 * 1.3 Configuration 등록
 *  - Configuration 클래스가 Spring Bean 정의를 제공한다
 * -------------------------------------------------------------------------
 * 2. Bean 등록 & 의존성 주입
 * 
 * 2.1 Bean 목록
 *  - 스프링 컨테이너에 등록되는 Bean: ItemRepository, ItemService, ItemController...
 *  
 * 2.2 의존성 주입 
 * -------------------------------------------------------------------------
 * 3. 웹 서버 시작
 *  - Spring Boot 내장 톰캣 서버가 시작
 *  - HTTP 요청을 받을 준비
 *  - 스프링 MVC는 @Controller 가 붙은 ItemController를 등록
 *  - @RequestMapping("/items")에 따라 /items URL 매핑이 준비됨
 *  
 */

@Slf4j
@Import(V2Config.class)
//@Import(QuerydslConfig.class)
//@Import(SpringDataJpaConfig.class)
//@Import(JpaConfig.class)
//@Import(MyBatisConfig.class)
//@Import(JdbcTemplateV3Config.class)
//@Import(JdbcTemplateV2Config.class)
//@Import(JdbcTemplateV1Config.class)
//@Import(MemoryConfig.class)
@SpringBootApplication(scanBasePackages = "hello.springdb2.web")
public class Springdb2Application {

    /**
     * 애플리케이션 실행 진입점(Main Method)
     * SpringApplication.run(...) 호출로 Spring Boot 애플리케이션 실행
     */
    public static void main(String[] args) {
        SpringApplication.run(Springdb2Application.class, args);
    }

    /**
     * @Bean
     *  - 메서드 반환 객체를 Spring Bean으로 등록.
     *  
     * @Profile("local")
     *  - "local" 프로파일이 활성화된 경우에만 Bean 등록.
     *    → 운영 환경(production)에서는 초기 데이터가 자동으로 생성되지 않음.
     */
    @Bean
    @Profile("local")
    TestDataInit testDataInit(ItemRepository itemRepository) {
        return new TestDataInit(itemRepository);
    }
    
//    @Bean
//    @Profile("test")
//    DataSource dataSource1() {
//        log.info("데이터베이스 초기화: testcase.mv.db");
//
//        HikariConfig config = new HikariConfig();
//        config.setDriverClassName("org.h2.Driver");
//        config.setJdbcUrl("jdbc:h2:file:~/testcase");
//        config.setUsername("sa");
//        config.setPassword("");
//        
//        // HikariCP 최적화 설정 (선택사항)
//        config.setMaximumPoolSize(10);
//        config.setMinimumIdle(2);
//        config.setPoolName("TestHikariCP");
//
//        return new HikariDataSource(config);
//    }
//    
//    @Bean
//    @Profile("test")
//    DataSource dataSource2() {
//        log.info("메모리 데이터베이스 초기화");
//        DriverManagerDataSource dataSource = new DriverManagerDataSource();
//        
//        dataSource.setDriverClassName("org.h2.Driver");
//        dataSource.setUrl("jdbc:h2:mem:db;DB_CLOSE_DELAY=-1");
//        dataSource.setUsername("sa");
//        dataSource.setPassword("");
//        
//        return dataSource;
//    }
    
}
