package seondays.shareticon.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import seondays.shareticon.login.CustomOAuth2User;
import seondays.shareticon.user.dto.UserProfileChangeRequest;
import seondays.shareticon.user.dto.UserProfileResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/profile")
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails.getId();
        UserProfileResponse result = userService.getUserProfile(userId);

        return ResponseEntity.ok(result);
    }

    @PatchMapping
    public ResponseEntity<Void> changeUserProfile(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @RequestBody @Valid UserProfileChangeRequest request) {
        Long userId = userDetails.getId();
        userService.changeUserProfile(userId, request);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
