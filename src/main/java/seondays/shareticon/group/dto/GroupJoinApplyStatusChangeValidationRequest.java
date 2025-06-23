package seondays.shareticon.group.dto;

import lombok.Builder;
import seondays.shareticon.group.Group;

@Builder
public record GroupJoinApplyStatusChangeValidationRequest(Long leaderId,
                                                          Long targetUserId,
                                                          Group targetGroup) {

}
