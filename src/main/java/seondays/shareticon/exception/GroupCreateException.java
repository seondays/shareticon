package seondays.shareticon.exception;

public class GroupCreateException extends RuntimeException {

    public GroupCreateException() {
        super("그룹 생성이 실패했습니다");
    }
}
