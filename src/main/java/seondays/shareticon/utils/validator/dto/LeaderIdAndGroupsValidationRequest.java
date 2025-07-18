package seondays.shareticon.utils.validator.dto;

import java.util.List;
import lombok.Builder;
import seondays.shareticon.group.Group;

@Builder
public record LeaderIdAndGroupsValidationRequest (Long leaderId,
                                                  List<Group> targetGroups){
    public static LeaderIdAndGroupsValidationRequest of(Long leaderId, List<Group> targetGroups) {
        return LeaderIdAndGroupsValidationRequest.builder()
                .leaderId(leaderId)
                .targetGroups(targetGroups).build();
    }
}
