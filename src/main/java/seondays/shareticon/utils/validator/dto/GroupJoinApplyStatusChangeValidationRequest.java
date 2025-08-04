package seondays.shareticon.utils.validator.dto;

import lombok.Builder;
import seondays.shareticon.group.Group;

@Builder
public record GroupJoinApplyStatusChangeValidationRequest(Long leaderId,
                                                          Long targetUserId,
                                                          Group targetGroup) {
    public static GroupJoinApplyStatusChangeValidationRequest of
            (Long leaderId, Long targetUserId, Group targetGroup) {
        return GroupJoinApplyStatusChangeValidationRequest.builder()
                .leaderId(leaderId)
                .targetUserId(targetUserId)
                .targetGroup(targetGroup).build();
    }
}
