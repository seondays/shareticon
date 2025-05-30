package seondays.shareticon.group.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreateGroupRequest(
        @NotNull(message = "그룹 이름을 포함해야 합니다")
        String title) {
}
