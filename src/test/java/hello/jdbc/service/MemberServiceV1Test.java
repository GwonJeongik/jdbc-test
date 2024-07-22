package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV1;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connect.ConnectionConst.*;
import static org.assertj.core.api.Assertions.*;

class MemberServiceV1Test {

    private static final String MEMBER_A = "memberA";
    private static final String MEMBER_B = "memberB";
    private static final String MEMBER_EX = "ex";

    private MemberRepositoryV1 repositoryV1;
    private MemberServiceV1 memberServiceV1;

    @BeforeEach
    void beforeEach() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        repositoryV1 = new MemberRepositoryV1(dataSource);
        memberServiceV1 = new MemberServiceV1(repositoryV1);
    }

    @AfterEach
    void afterEach() throws SQLException {
        repositoryV1.delete(MEMBER_A);
        repositoryV1.delete(MEMBER_B);
        repositoryV1.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given

        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        repositoryV1.save(memberA);
        repositoryV1.save(memberB);

        //when
        memberServiceV1.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        
        //then
        Member findMemberA = repositoryV1.findById(memberA.getMemberId());
        Member findMemberB = repositoryV1.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);

        repositoryV1.save(memberA);
        repositoryV1.save(memberEx);

        //when
        assertThatThrownBy(() -> memberServiceV1.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = repositoryV1.findById(memberA.getMemberId());
        Member findMemberEx = repositoryV1.findById(memberEx.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }
}