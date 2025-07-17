package seondays.shareticon.group.dto;

import lombok.Builder;

@Builder
public record UserIdAndGroupIdValidationRequest(Long requestUserId,
                                                Long targetGroupId) {

}
