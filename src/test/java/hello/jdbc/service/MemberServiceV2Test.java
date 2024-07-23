package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.connect.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberServiceV2Test {

    private static final String MEMBER_A = "memberA";
    private static final String MEMBER_B = "memberB";
    private static final String MEMBER_EX = "ex";

    private MemberRepositoryV2 repositoryV2;
    private MemberServiceV2 memberServiceV2;

    @BeforeEach
    void beforeEach() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        repositoryV2 = new MemberRepositoryV2(dataSource);
        memberServiceV2 = new MemberServiceV2(dataSource, repositoryV2);
    }

    @AfterEach
    void afterEach() throws SQLException {
        repositoryV2.delete(MEMBER_A);
        repositoryV2.delete(MEMBER_B);
        repositoryV2.delete(MEMBER_EX);
    }

    @Test
    @DisplayName("정상 이체")
    void accountTransfer() throws SQLException {
        //given

        Member memberA = new Member(MEMBER_A, 10000);
        Member memberB = new Member(MEMBER_B, 10000);
        repositoryV2.save(memberA);
        repositoryV2.save(memberB);

        //when
        memberServiceV2.accountTransfer(memberA.getMemberId(), memberB.getMemberId(), 2000);
        
        //then
        Member findMemberA = repositoryV2.findById(memberA.getMemberId());
        Member findMemberB = repositoryV2.findById(memberB.getMemberId());
        assertThat(findMemberA.getMoney()).isEqualTo(8000);
        assertThat(findMemberB.getMoney()).isEqualTo(12000);
    }

    @Test
    @DisplayName("이체중 예외 발생")
    void accountTransferEx() throws SQLException {
        //given
        Member memberA = new Member(MEMBER_A, 10000);
        Member memberEx = new Member(MEMBER_EX, 10000);

        repositoryV2.save(memberA);
        repositoryV2.save(memberEx);

        //when
        assertThatThrownBy(() -> memberServiceV2.accountTransfer(memberA.getMemberId(), memberEx.getMemberId(), 2000))
                .isInstanceOf(IllegalStateException.class);

        //then
        Member findMemberA = repositoryV2.findById(memberA.getMemberId());
        Member findMemberEx = repositoryV2.findById(memberEx.getMemberId());

        assertThat(findMemberA.getMoney()).isEqualTo(10000);
        assertThat(findMemberEx.getMoney()).isEqualTo(10000);
    }
}