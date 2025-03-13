package seondays.shareticon.exception;

public class GroupNotFoundException extends RuntimeException {

    public GroupNotFoundException() {
        super("해당 그룹을 찾을 수 없습니다");
    }
}
