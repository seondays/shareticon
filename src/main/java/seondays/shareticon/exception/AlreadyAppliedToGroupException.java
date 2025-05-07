package seondays.shareticon.exception;

public class AlreadyAppliedToGroupException extends RuntimeException {

    public AlreadyAppliedToGroupException() {
        super("이미 그룹에 가입했거나 가입 신청 대기 중입니다");
    }
}
