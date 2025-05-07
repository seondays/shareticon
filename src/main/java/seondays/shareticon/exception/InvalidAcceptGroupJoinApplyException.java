package seondays.shareticon.exception;

public class InvalidAcceptGroupJoinApplyException extends RuntimeException {

    public InvalidAcceptGroupJoinApplyException() {
        super("그룹 가입을 수락할 권한이 없습니다. 수락은 그룹 리더만 가능합니다");
    }
}
