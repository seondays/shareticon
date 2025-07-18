package seondays.shareticon.exception;

public class InvalidGroupLeaderException extends RuntimeException {

    public InvalidGroupLeaderException() {
        super("그룹 관리 작업의 권한이 없습니다. 해당 그룹의 리더 유저만 가능합니다");
    }
}
