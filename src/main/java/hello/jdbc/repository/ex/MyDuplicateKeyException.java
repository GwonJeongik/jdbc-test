package hello.jdbc.repository.ex;

/**
 * MyDbException을 상속받음 -> DB오류라는 카테고리로 묶을 수 있음 -> 의미있는 계층 형성
 */
public class MyDuplicateKeyException extends MyDbException{

    public MyDuplicateKeyException() {
        super();
    }

    public MyDuplicateKeyException(String message) {
        super(message);
    }

    public MyDuplicateKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicateKeyException(Throwable cause) {
        super(cause);
    }
}
