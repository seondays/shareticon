package seondays.shareticon.group;

import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seondays.shareticon.group.dto.GroupListResponse;
import seondays.shareticon.login.CustomOAuth2User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<Void> createGroup(@AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails.getId();
        Group createGroupId = groupService.createGroup(userId);

        return ResponseEntity.created(URI.create("/group/" + createGroupId.getId())).build();
    }

    @GetMapping
    public ResponseEntity<List<GroupListResponse>> getAllGroupList(
            @AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails.getId();
        List<GroupListResponse> responseList = groupService.getAllGroupList(userId);
        return ResponseEntity.ok().body(responseList);
    }
}
