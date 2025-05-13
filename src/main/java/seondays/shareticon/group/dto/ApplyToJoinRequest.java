package seondays.shareticon.group.dto;

import jakarta.validation.constraints.NotEmpty;

public record ApplyToJoinRequest(@NotEmpty String inviteCode) {

}
