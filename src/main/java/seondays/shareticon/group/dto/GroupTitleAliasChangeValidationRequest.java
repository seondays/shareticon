package seondays.shareticon.group.dto;

import lombok.Builder;

@Builder
public record GroupTitleAliasChangeValidationRequest(Long requestUserId,
                                                     Long targetGroupId) {

}
