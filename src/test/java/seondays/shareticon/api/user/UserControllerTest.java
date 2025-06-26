package seondays.shareticon.api.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seondays.shareticon.api.config.ControllerTestSupport;
import seondays.shareticon.login.CustomOAuth2User;
import seondays.shareticon.user.dto.UserOAuth2Dto;
import seondays.shareticon.user.dto.UserProfileResponse;

public class UserControllerTest extends ControllerTestSupport {

    @BeforeEach
    void setup() {
        mockUser = new CustomOAuth2User(new UserOAuth2Dto(1L, "user", "ROLE_USER"));
    }

    @Test
    @DisplayName("유저 프로필 조회 시 결과에는 유저 닉네임, 유저 이메일, 유저가 가입한 그룹 카운트, 유저가 등록한 기프티콘 카운트가 포함된다")
    void getUserProfile() throws Exception {
        //given
        String nickname = "닉네임";
        String email = "test@test";
        Long joinGroupCount = 1L;
        Long ownedVoucherCount = 2L;

        UserProfileResponse mockResponse = UserProfileResponse.builder().nickName(nickname)
                .email(email).joinGroupCount(joinGroupCount).ownedVoucherCount(ownedVoucherCount).build();

        //when
        when(userService.getUserProfile(any(Long.class))).thenReturn(mockResponse);

        //then
        mockMvc.perform(
                MockMvcRequestBuilders.get("/profile")
                        .with(csrf())
                        .with(oauth2Login().oauth2User(mockUser))
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.nickName").value(nickname))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.joinGroupCount").value(joinGroupCount))
                .andExpect(jsonPath("$.ownedVoucherCount").value(ownedVoucherCount));

    }

}
