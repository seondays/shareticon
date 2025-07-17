package seondays.shareticon.group.dto;

import java.util.List;
import lombok.Builder;
import seondays.shareticon.group.Group;

@Builder
public record LeaderIdAndGroupsValidationRequest (Long leaderId,
                                                  List<Group> targetGroups){

}
