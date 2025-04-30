package seondays.shareticon.login.token;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import seondays.shareticon.login.CustomOAuth2User;

@RestController
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @GetMapping("/reissue")
    public ResponseEntity<Void> reissueAccessToken(HttpServletRequest request,
            HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        String accessToken = tokenService.reissueAccessToken(cookies);
        response.setHeader("Authorization", accessToken);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal CustomOAuth2User userDetails) {
        Long userId = userDetails.getId();
        tokenService.deleteRefreshToken(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
