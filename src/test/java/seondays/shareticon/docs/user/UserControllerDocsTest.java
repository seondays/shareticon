package seondays.shareticon.docs.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seondays.shareticon.docs.RestDocsSupport;
import seondays.shareticon.user.UserController;
import seondays.shareticon.user.UserService;
import seondays.shareticon.user.dto.UserProfileChangeRequest;
import seondays.shareticon.user.dto.UserProfileResponse;

public class UserControllerDocsTest extends RestDocsSupport {

    private final UserService userService = mock(UserService.class);

    @Override
    protected Object initController() {
        return new UserController(userService);
    }

    @Test
    @DisplayName("유저 프로필 조회 시 결과에는 유저 아이디, 유저 닉네임, 유저 이메일, 유저가 가입한 그룹 카운트, 유저가 등록한 기프티콘 카운트가 포함된다")
    void getUserProfile() throws Exception {
        //given
        Long userId = mockUser.getId();
        String nickname = "닉네임";
        String email = "test@test";
        Long joinGroupCount = 1L;
        Long ownedVoucherCount = 2L;

        UserProfileResponse mockResponse = UserProfileResponse.builder().userId(userId).nickName(nickname)
                .email(email).joinGroupCount(joinGroupCount).ownedVoucherCount(ownedVoucherCount)
                .build();

        //when
        when(userService.getUserProfile(any(Long.class))).thenReturn(mockResponse);

        //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/profile")
                                .with(addBearerToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.nickName").value(nickname))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.joinGroupCount").value(1))
                .andExpect(jsonPath("$.ownedVoucherCount").value(2))
                .andDo(document("user-profile",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("userId").type(JsonFieldType.NUMBER).description("서버에서의 유저 아이디"),
                                fieldWithPath("nickName").type(JsonFieldType.STRING).description("유저의 닉네임"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("유저의 이메일"),
                                fieldWithPath("joinGroupCount").type(JsonFieldType.NUMBER).description("유저가 가입되어 있는 그룹의 개수"),
                                fieldWithPath("ownedVoucherCount").type(JsonFieldType.NUMBER).description("유저가 등록한 쿠폰의 총 개수")
                        )));

    }

    @Test
    @DisplayName("유효한 닉네임으로 유저 프로필을 변경한다")
    void changeUserProfile() throws Exception {
        //given
        String newNickname = "새로운 닉네임";

        UserProfileChangeRequest request = UserProfileChangeRequest
                .builder()
                .newNickname(newNickname)
                .build();
        String jsonRequest = objectMapper.writeValueAsString(request);

        //when
        doNothing().when(userService)
                .changeUserProfile(any(Long.class), any(UserProfileChangeRequest.class));

        //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/profile")
                                .with(addBearerToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonRequest)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNoContent())
                .andDo(document("change-user-profile",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestFields(
                                fieldWithPath("newNickname").type(JsonFieldType.STRING).description("유저가 새로 바꾸려는 닉네임")
                        )));

    }
}
