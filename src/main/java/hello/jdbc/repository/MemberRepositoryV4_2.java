package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 * 스프링 데이터 접근 예외로 변환
 * SQLExceptionTranslator 추가
 */

@Slf4j
public class MemberRepositoryV4_2 implements MemberRepository {

    private final DataSource dataSource;
    private final SQLExceptionTranslator exceptionTranslator;

    public MemberRepositoryV4_2(DataSource dataSource) {
        this.dataSource = dataSource;
        this.exceptionTranslator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
    }

    /**
     * 데이터 베이스에 넘길 sql
     * 커넥션 획득
     * 획득한 커넥션으로 데이터베이스에 전달할 sql, 데이터 준비
     * 데이터 세팅하고 실행
     * 자원 연결 해제
     */
    @Override
    public Member save(Member member) {
        String sql = "insert into member(member_id, money) values(?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;


        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2, member.getMoney());
            pstmt.executeUpdate();
            return member;
        } catch (SQLException e) {
            throw exceptionTranslator.translate("save", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    /**
     * member_id로 Member 조회
     * 조회할 sql문 작성
     * Connect con, prepareStatement pstmt, ResultSet rs
     * 커넥터 찾아오고, (sql 과 조회에 필요한, 매개변수로 넘길 데이터를 보관), 데이터 베이스 조회 결과
     * cursor이 데이터 지정하게 이동
     */
    @Override
    public Member findById(String memberId) {
        String sql = "select * from member where member_id = ?"; // 조회하는 sql문

        // finally에서 호출해야하기 때문에 try, catch 밖에 선언해놓았다.
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection(); //커넥트 가져오기

            pstmt = con.prepareStatement(sql); // 조회 sql을 pstmt가 가지고있음
            pstmt.setString(1, memberId); // 매개변수로 넘길 데이터 전달 / 첫 번째 '?'에 memberId 전달

            rs = pstmt.executeQuery(); // ResultSet은 데이터 조회한 결과를 담는 통 같은 거다.

            // rs의 cursor는 아무 데이터도 가리키고 있지않다. 그래서 next()로 커서를 다음위치로 이동시킨다. 데이터가 있으면 true. 한 번은 해줘야 데이터 조회가 가능하다.
            if (rs.next()) {
                Member member = new Member(); // 조회한 데이터를 담을 member을 생성 // x002
                member.setMemberId(rs.getString("member_id")); // 조회할 컬럼명을 입력해서 String타입으로 값을 꺼낸다.
                member.setMoney(rs.getInt("money")); // 조회할 컬럼명을 입력해서 int형으로 값을 꺼낸다.
                return member; // 조회된 값이 들어간 member를 반환

            } else { // rs의 cursor가 다음에 가리키는 데이터가 없으면 여기로 온다. -> rs.next()가 false
                throw new NoSuchElementException();
            }

        } catch (SQLException e) {
            throw exceptionTranslator.translate("findById", sql, e);

        } finally {
            close(con, pstmt, rs); // 사용한 자원의 연결을 끊는다. -> 사용한 역순으로
        }
    }

    /**
     * update sql 작성
     * Connection con, PreparedStatement pstmt
     * 커넥션 획득
     * 실행할 sql 전달
     * 매개변수로 줄 데이터 전달
     * 데이터 베이스에서 필요한 부분 변경
     * 자원 끊기
     */
    @Override
    public void update(String memberId, int money) {
        String sql = "update member set money=? where member_id=?"; // sql injection 공격 막으려 바인딩

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();

            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money); // sql injection 공격 막으려 바인딩
            pstmt.setString(2, memberId); // sql injection 공격 막으려 바인딩

            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);

        } catch (SQLException e) {
            throw exceptionTranslator.translate("update", sql, e);

        } finally {
            close(con, pstmt, null);
        }
    }

    /**
     * delete sql 작성
     * Connection con, PreparedStatement pstmt
     * 커넥션 획득
     * 삭제 sql, 데이터 전달
     * 실행
     * 자원 끊기
     */
    @Override
    public void delete(String memberId) {
        String sql = "delete from member where member_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();

            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw exceptionTranslator.translate("delete", sql, e);
        } finally {
            close(con, pstmt, null);
        }
    }

    private Connection getConnection() throws SQLException {

        Connection con = DataSourceUtils.getConnection(dataSource); //**트랜잭션 동기화 사용을 위해서 DataSourceUtils를 사용**
        log.info("connection={} class={}", con, con.getClass());
        return con;
    }

    private void close(Connection con, PreparedStatement pstmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(pstmt);
        DataSourceUtils.releaseConnection(con, dataSource); // 트랜잭션을 사용하는 커넥션이면 연결을 끊고, 아니면 그냥 둔다.
    }
}
