package seondays.shareticon.api.login.token;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seondays.shareticon.api.config.ControllerTestSupport;
import seondays.shareticon.login.CustomOAuth2User;
import seondays.shareticon.user.dto.UserOAuth2Dto;

public class TokenControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("리프레시 토큰으로 엑세스 토큰을 요청한다")
    void reissueAccessToken() throws Exception {
        //given
        String accessToken = "Bearer expectedToken";

        given(tokenService.reissueAccessToken(any(Cookie[].class))).willReturn(accessToken);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/reissue")
                                .cookie(new Cookie("refresh", "refreshtoken"))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().string("Authorization", accessToken));
    }

    @Test
    @DisplayName("엑세스 토큰 요청 시에는 리프레시 토큰이 있어야 한다")
    void reissueAccessTokenWithNoRefreshToken() throws Exception {
        // given
        given(tokenService.reissueAccessToken(isNull()))
                .willThrow(new BadCredentialsException("잘못된 리프레시 토큰입니다."));

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/reissue")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다"))
                .andExpect(jsonPath("$.code").value("401"));
    }

    @Test
    @DisplayName("로그인 한 상태에서 로그아웃을 요청한다")
    void requestLogoutWithAccessToken() throws Exception {
        //given
        CustomOAuth2User mockUser = new CustomOAuth2User(
                new UserOAuth2Dto(1L, "user", "ROLE_USER"));

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/logout")
                                .with(csrf())
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("로그인 되어 있지 않은 상황에서 로그아웃 요청 시 예외가 발생한다")
    void requestLogoutWithNoLogin() throws Exception {
        //given

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/logout")
                                .with(csrf())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
}
