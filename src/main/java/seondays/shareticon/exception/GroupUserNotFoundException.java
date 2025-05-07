package seondays.shareticon.exception;

public class GroupUserNotFoundException extends RuntimeException {

    public GroupUserNotFoundException() {
        super("해당 그룹과 유저에 대한 정보가 존재하지 않습니다");
    }
}
