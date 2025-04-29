package seondays.shareticon.docs.token;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName;
import static org.springframework.restdocs.cookies.CookieDocumentation.requestCookies;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seondays.shareticon.docs.RestDocsSupport;
import seondays.shareticon.login.token.TokenController;
import seondays.shareticon.login.token.TokenService;

public class TokenControllerDocsTest extends RestDocsSupport {

    private final TokenService tokenService = mock(TokenService.class);

    @Override
    protected Object initController() {
        return new TokenController(tokenService);
    }

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
                .andExpect(MockMvcResultMatchers.header().string("Authorization", accessToken))
                .andDo(document("token-reissue",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestCookies(cookieWithName("refresh").description("리프레시 토큰")),
                        responseHeaders(
                                headerWithName("Authorization").description("엑세스 토큰"))
                        ));
    }

}
