package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepository;

    public MemberServiceV2(DataSource dateSource, MemberRepositoryV2 memberRepository) {
        this.dataSource = dateSource;
        this.memberRepository = memberRepository;
    }

    /**
     * 보내는 사람
     * 받는 사람을 찾아야한다.
     * 보내는 사람은 현재 잔액에서 (-)
     * 받는 사람은 현재 잔액에서 (+) 처리한다.
     */
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        Connection con = dataSource.getConnection(); //커넥션 획득

        // 비지니스 로직과 데이터 베이스 로직이 섞여있는 문제!
        try{
            con.setAutoCommit(false); //트랜잭션 시작
            bizLogic(con, fromId, toId, money); //비지니스 로직
            con.commit(); //성공시 커밋
        } catch (Exception e) {
            con.rollback(); //실패시 롤백
            throw new IllegalStateException(e);
        } finally {
            release(con);
        }

    }

    private void validation(Member toMember) {
        if(toMember.getMemberId().equals("ex")){
            throw new IllegalStateException("이체중 예외 발생");
                    }
    }

    private void bizLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(con, fromId, (fromMember.getMoney() - money));
        validation(toMember);
        memberRepository.update(con, toId, (toMember.getMoney() + money));
    }


    private void release(Connection con) throws SQLException {
        con.setAutoCommit(true); // 커넥션 풀에 반납하기 전에 오토커밋으로 변경
        con.close(); // 커넥션 반납
    }


}
