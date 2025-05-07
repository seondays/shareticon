package seondays.shareticon.exception;

public class InvalidJoinGroupException extends RuntimeException {

    public InvalidJoinGroupException() {
        super("그룹 가입 대기중인 상태가 아닙니다");
    }
}
