package seondays.shareticon.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

@Builder
public record UserProfileChangeRequest(
        @NotEmpty(message = "변경할 닉네임을 포함해야 합니다") String newNickname) {

}
