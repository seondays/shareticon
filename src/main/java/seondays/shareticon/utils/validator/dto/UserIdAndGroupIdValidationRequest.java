package seondays.shareticon.utils.validator.dto;

import lombok.Builder;

@Builder
public record UserIdAndGroupIdValidationRequest(Long targetUserId,
                                                Long targetGroupId) {

    public static UserIdAndGroupIdValidationRequest of(Long targetUserId, Long targetGroupId) {
        return UserIdAndGroupIdValidationRequest.builder()
                .targetUserId(targetUserId)
                .targetGroupId(targetGroupId).build();
    }
}
