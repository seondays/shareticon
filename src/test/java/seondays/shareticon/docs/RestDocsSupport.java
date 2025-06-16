package seondays.shareticon.docs;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import seondays.shareticon.login.CustomOAuth2User;
import seondays.shareticon.user.dto.UserOAuth2Dto;

@ExtendWith(RestDocumentationExtension.class)
public abstract class RestDocsSupport {

    protected MockMvc mockMvc;
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected CustomOAuth2User mockUser;
    protected Authentication auth;

    @BeforeEach
    void setUp(RestDocumentationContextProvider provider) {
        // objectMapper가 LocalDate YYYY-MM-DD 형식을 직렬화 할 수 있도록 설정 추가
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // setMessageConverters를 통해 mockMvc의 LocalDate 역직렬화를 위한 컨버터 추가
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
                .apply(documentationConfiguration(provider))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();

        mockUser = new CustomOAuth2User(new UserOAuth2Dto(1L, "user", "ROLE_USER"));
        auth = new UsernamePasswordAuthenticationToken(
                mockUser, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    protected abstract Object initController();
}
