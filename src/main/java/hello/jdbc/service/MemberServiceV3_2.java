package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 템플릿
 * 트랜잭션 시작 - 종료의 반복되는 부분 해결.
 */

public class MemberServiceV3_2 {

    private final TransactionTemplate txTemplate;
    private final MemberRepositoryV3 memberRepository;

    public MemberServiceV3_2(PlatformTransactionManager transactionManager, MemberRepositoryV3 memberRepository) {
        this.txTemplate = new TransactionTemplate(transactionManager);
        this.memberRepository = memberRepository;
    }

    /**
     * 보내는 사람
     * 받는 사람을 찾아야한다.
     * 보내는 사람은 현재 잔액에서 (-)
     * 받는 사람은 현재 잔액에서 (+) 처리한다.
     */
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        txTemplate.executeWithoutResult((transactionStatus -> {
            try {
                bizLogic(fromId, toId, money); //비지니스 로직
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        }));
    }

    private void validation(Member toMember) {
        if (toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, (fromMember.getMoney() - money));
        validation(toMember);
        memberRepository.update(toId, (toMember.getMoney() + money));
    }


    private void release(Connection con) throws SQLException {
        con.setAutoCommit(true); // 커넥션 풀에 반납하기 전에 오토커밋으로 변경
        con.close(); // 커넥션 반납
    }


}
