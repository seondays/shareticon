package seondays.shareticon.group.dto;

public record GroupListResponse(Long groupId,
                                String groupTitleAlias,
                                Long memberCount) {

    public GroupListResponse {}
}
