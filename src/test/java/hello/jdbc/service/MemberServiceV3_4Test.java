package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * DataSource(기본 HikariDataSource), TransactionManager(라이브러리를 보고판단) 자동 등록(스프링 부트)
 */

@Slf4j
@SpringBootTest
class MemberServiceV3_4Test {

    private static final String MEMBER_A = "memberA";
    private static final String MEMBER_B = "memberB";
    private static final String MEMBER_EX = "ex";

    private MemberRepositoryV3 repositoryV3;
    private MemberServiceV3_3 memberServiceV3_3;
    @Qualifier("dataSource")
    @Autowired
    private DataSource dataSource;
    @Autowired
    private DataSourceTransactionManager transactionManager;

    @Autowired
    public MemberServiceV3_4Test(MemberRepositoryV3 repositoryV3, MemberServiceV3_3 memberServiceV3_3) {
        this.repositoryV3 = repositoryV3;
        this.memberServiceV3_3 = memberServiceV3_3;
    }

    @TestConfiguration
    static class testConfig {

        private final DataSource dataSource;
        private final PlatformTransactionManager transactionManager;

        @Autowired // 생성자가 1개 있으므로, 생략 가능
        testConfig(DataSource dataSource, PlatformTransactionManager transactionManager) {
            this.dataSource = dataSource;
            this.transactionManager = transactionManager;
        }

        @PostConstruct
        void postConstruct() {
            log.info("DataSource class={}", dataSource.getClass());
            log.info("TransactionManager class={}", transactionManager.getClass());
        }


        @Bean
        MemberRepositoryV3 memberRepositoryV3() {
            return new MemberRepositoryV3(dataSource);
        }

        @Bean
        MemberServiceV3_3 memberServiceV3_3() {
            return new MemberServiceV3_3(memberRepositoryV3());
        }

    }

    @AfterEach
    void afterEach() throws SQLException {
        repositoryV3.delete(MEMBER_A);
        repositoryV3.delete(MEMBER_B);
        repositoryV3.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("트랜잭션 AOP 적용 확인")
    void AopCheck() {
        log.info("MemberRepositoryV3 class={}", repositoryV3.getClass());
        log.info("MemberServiceV3_3 class={}", memberServiceV3_3.getClass());
        assertThat(AopUtils.isAopProxy(memberServiceV3_3)).isTrue();
        assertThat(AopUtils.isAopProxy(repositoryV3)).isFalse();


    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given

        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        repositoryV3.save(memberA);
        repositoryV3.save(memberB);

        //when
        memberServiceV3_3.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);

        //then
        Member findMemberA = repositoryV3.findById(memberA.getMemberId());
        Member findMemberB = repositoryV3.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);

        repositoryV3.save(memberA);
        repositoryV3.save(memberEx);

        //when
        assertThatThrownBy(() -> memberServiceV3_3.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = repositoryV3.findById(memberA.getMemberId());
        Member findMemberEx = repositoryV3.findById(memberEx.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }
}