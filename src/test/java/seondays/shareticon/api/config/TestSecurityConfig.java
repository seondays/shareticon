package seondays.shareticon.api.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                auth -> auth.requestMatchers("/", "/reissue", "/error", "/oauth2**",
                                "/swagger-ui/**", "/v3/api-docs/**", "/api-docs/**")
                        .permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }
}
