package seondays.shareticon.group;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<Void> applyToJoinGroup(@Valid @RequestBody ApplyToJoinRequest request,
            @AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails.getId();
        groupService.applyToJoinGroup(request, userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/join-requests")
    public ResponseEntity<List<ApplyToJoinResponse>> getAllApplyToJoinList(@AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails.getId();
        List<ApplyToJoinResponse> responseList = groupService.getAllApplyToJoinList(userId);
        return ResponseEntity.ok().body(responseList);
    }

    @PatchMapping("/{groupId}/user/{userId}")
    public ResponseEntity<Void> changeJoinApplyStatus(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @PathVariable("groupId") Long targetGroupId,
            @PathVariable("userId") Long targetUserId,
            @RequestParam("status") ApprovalStatus approvalStatus) {
        Long leaderId = userDetails.getId();
        groupService.changeJoinApplyStatus(targetGroupId, targetUserId, leaderId, approvalStatus);
        return ResponseEntity.ok().build();
    }
}
