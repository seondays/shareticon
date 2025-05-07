package seondays.shareticon.group;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum JoinStatus {
    JOINED("가입 중"),
    PENDING("가입 대기 중"),
    REJECTED("가입 거절됨"),
    WITHDRAWN("그룹 탈퇴");

    private final String discription;

    public static boolean isAlreadyApplied(JoinStatus status) {
        return status == JOINED || status == PENDING;
    }

    public static boolean isWaitingAcceptJoinApply(JoinStatus status) {
        return status == PENDING;
    }
}
