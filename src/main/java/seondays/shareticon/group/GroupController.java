package seondays.shareticon.group;

import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seondays.shareticon.group.dto.ApplyToJoinRequest;
import seondays.shareticon.group.dto.ApplyToJoinResponse;
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

    @PostMapping("/join")
    public ResponseEntity<Void> registerInvitation(@RequestBody ApplyToJoinRequest request,
            @AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails.getId();
        groupService.applyToJoinGroup(request, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    // 리더가 본인 그룹에 있는 가입 요청을 모두 확인할 수 있는 API
    @GetMapping("/applys")
    public ResponseEntity<List<ApplyToJoinResponse>> getAllApplyToJoinList(@AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails.getId();
        List<ApplyToJoinResponse> responseList = groupService.getAllApplyToJoinList(userId);
        return ResponseEntity.ok().body(responseList);
    }

    // 리더가 원하는 요청을 수락/거절하는 API
    @PatchMapping("/{groupId}/user/{userId}")
    public void acceptJoinApply(@AuthenticationPrincipal CustomOAuth2User userDetails,
            @PathVariable("groupId") Long targetGroupId, @PathVariable("userId") Long targetUserId) {
        Long leaderId = userDetails.getId();
        groupService.acceptJoinApply(targetGroupId, targetUserId, leaderId);
    }
}
