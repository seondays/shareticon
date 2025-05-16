package seondays.shareticon.group;

public enum ApprovalStatus {
    APPROVED,
    REJECTED;

    public static boolean isApproved(ApprovalStatus approvalStatus) {
        return APPROVED.equals(approvalStatus);
    }
}
