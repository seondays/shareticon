package seondays.shareticon.docs.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seondays.shareticon.docs.RestDocsSupport;
import seondays.shareticon.user.UserController;
import seondays.shareticon.user.UserService;
import seondays.shareticon.user.dto.UserProfileResponse;

public class UserControllerDocsTest extends RestDocsSupport {

    private final UserService userService = mock(UserService.class);

    @Override
    protected Object initController() {
        return new UserController(userService);
    }

    @Test
    @DisplayName("유저 프로필 조회 시 결과에는 유저 닉네임, 유저 이메일, 유저가 가입한 그룹 카운트, 유저가 등록한 기프티콘 카운트가 포함된다")
    void getUserProfile() throws Exception {
        //given
        String nickname = "닉네임";
        String email = "test@test";

        UserProfileResponse mockResponse = UserProfileResponse.builder().nickName(nickname)
                .email(email).joinGroupCount(1L).ownedVoucherCount(2L).build();

        //when
        when(userService.getUserProfile(any(Long.class))).thenReturn(mockResponse);

        //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/profile")
                                .with(addBearerToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.nickName").value(nickname))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.joinGroupCount").value(1))
                .andExpect(jsonPath("$.ownedVoucherCount").value(2))
                .andDo(document("user-profile",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("nickName").type(JsonFieldType.STRING).description("유저의 닉네임"),
                                fieldWithPath("email").type(JsonFieldType.STRING).description("유저의 이메일"),
                                fieldWithPath("joinGroupCount").type(JsonFieldType.NUMBER).description("유저가 가입되어 있는 그룹의 개수"),
                                fieldWithPath("ownedVoucherCount").type(JsonFieldType.NUMBER).description("유저가 등록한 쿠폰의 총 개수")
                        )));

    }
}
