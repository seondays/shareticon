package seondays.shareticon.group.dto;

public record GroupListResponse(Long groupId,
                                String groupTitleAlias,
                                int memberCount) {

    public GroupListResponse {}
}
