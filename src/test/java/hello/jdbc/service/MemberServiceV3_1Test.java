package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import java.sql.SQLException;

import static hello.jdbc.connect.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberServiceV3_1Test {

    private static final String MEMBER_A = "memberA";
    private static final String MEMBER_B = "memberB";
    private static final String MEMBER_EX = "ex";

    private MemberRepositoryV3 repositoryV3;
    private MemberServiceV3_1 memberServiceV3_1;

    @BeforeEach
    void beforeEach() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        repositoryV3 = new MemberRepositoryV3(dataSource);
        memberServiceV3_1 = new MemberServiceV3_1(transactionManager, repositoryV3);
    }

    @AfterEach
    void afterEach() throws SQLException {
        repositoryV3.delete(MEMBER_A);
        repositoryV3.delete(MEMBER_B);
        repositoryV3.delete(MEMBER_EX);
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
        memberServiceV3_1.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        
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
        assertThatThrownBy(() -> memberServiceV3_1.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = repositoryV3.findById(memberA.getMemberId());
        Member findMemberEx = repositoryV3.findById(memberEx.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }
}