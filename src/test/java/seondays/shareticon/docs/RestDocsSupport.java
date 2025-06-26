package seondays.shareticon.docs;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.validation.Validator;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
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
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;
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
        // DTO 검증을 수행할 validator에서 우리가 지정한 clock 객체를 사용하도록 직접 설정
        this.mockMvc = MockMvcBuilders.standaloneSetup(initController())
                .apply(documentationConfiguration(provider))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setValidator(validator(clock()))
                .build();

        mockUser = new CustomOAuth2User(new UserOAuth2Dto(1L, "user", "ROLE_USER"));
        auth = new UsernamePasswordAuthenticationToken(
                mockUser, mockUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    protected abstract Object initController();

    protected RequestPostProcessor addBearerToken() {
        return request -> {
            request.addHeader("Authorization", "Bearer TOKEN_VALUE");
            return request;
        };
    }

    private Validator validator(Clock clock) {
        ValidatorFactory factory = Validation.byDefaultProvider()
                .configure()
                .clockProvider(() -> clock)
                .buildValidatorFactory();
        return new SpringValidatorAdapter(factory.getValidator());
    }

    private Clock clock() {
        return Clock.fixed(Instant.parse("2024-12-01T00:00:00Z"), ZoneId.of("Asia/Seoul"));
    }
}
