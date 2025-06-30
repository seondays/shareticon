package seondays.shareticon.group.dto;

import java.util.List;

public record ApplyToJoinResponse(Long targetGroupId,
                                  String leaderGroupAlias,
                                  List<PendingMemberResponse> pendingMembers) {

    public static ApplyToJoinResponse of(Long targetGroupId, String leaderGroupAlias,
            List<PendingMemberResponse> pendingMembers) {
        return new ApplyToJoinResponse(targetGroupId, leaderGroupAlias, pendingMembers);
    }
}
