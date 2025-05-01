package seondays.shareticon.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import seondays.shareticon.login.JwtAuthenticationConverter;
import seondays.shareticon.login.LoginSuccessHandler;
import seondays.shareticon.login.OAuth2UserService;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;
    private final LoginSuccessHandler loginSuccessHandler;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final AuthenticationEntryPoint entryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // disable csrf, formLogin, basic, logout handler
        http.csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.logout(LogoutConfigurer::disable);

        // OAuth2
        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/oauth2/authorization/kakao")
                .userInfoEndpoint(
                        userInfoEndpointConfig -> userInfoEndpointConfig.userService(oAuth2UserService))
                .successHandler(loginSuccessHandler));

        // stateless
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 인증 경로 설정
        http.authorizeHttpRequests(
                auth -> auth.requestMatchers("/", "/reissue", "/error", "/oauth2**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**")
                        .permitAll()
                        .anyRequest().authenticated());

        // Resource Server 설정
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(
                        jwtAuthenticationConverter.convertToAuthentication()))
                        .authenticationEntryPoint(entryPoint));

        return http.build();
    }
}
