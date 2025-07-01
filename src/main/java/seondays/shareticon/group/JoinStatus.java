package seondays.shareticon.group;

import java.util.List;
import lombok.RequiredArgsConstructor;
import seondays.shareticon.exception.AlreadyAppliedToGroupException;
import seondays.shareticon.exception.InvalidJoinGroupException;

@RequiredArgsConstructor
public enum JoinStatus {
    JOINED("가입 중"),
    PENDING("가입 대기 중"),
    REJECTED("가입 거절됨"),
    WITHDRAWN("그룹 탈퇴");

    private final String discription;

    public void validateAlreadyApplied() {
        if (this.equals(JOINED) || this.equals(PENDING)) {
            throw new AlreadyAppliedToGroupException();
        }
    }

    public void validateWaitingAcceptJoinApply() {
        if (!this.equals(PENDING)) {
            throw new InvalidJoinGroupException();
        }
    }

    public static List<JoinStatus> getStatusesForAcceptedGroupMembers() {
        return List.of(JOINED);
    }
}
