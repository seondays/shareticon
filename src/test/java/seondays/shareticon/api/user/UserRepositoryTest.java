package seondays.shareticon.api.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seondays.shareticon.api.config.RepositoryTestSupport;
import seondays.shareticon.login.OAuth2Type;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;

public class UserRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private UserRepository userRepository;
    
    @Test
    @DisplayName("oAuth2 id와 oAuth2 프로바이더 타입으로 유저를 조회한다")
    void getUserWithoAuth2IdAndoAuth2Provider() {
        //given
        User user = User.builder().oauth2Id("1234").oauth2Type(OAuth2Type.KAKAO).build();
        userRepository.save(user);

        // when
        Optional<User> result = userRepository.findByOauth2IdAndOauth2Type(
                user.getOauth2Id(), user.getOauth2Type());

        // then
        assertThat(result).isPresent()
                .get()
                .extracting(User::getId, User::getOauth2Id, User::getOauth2Type)
                .containsExactly(user.getId(), "1234", OAuth2Type.KAKAO);
    }

    @Test
    @DisplayName("유저를 새로 저장할 때, 생성된 날짜와 시간이 함께 저장된다")
    void auditingUser() {
        // given
        User user = User.builder().oauth2Id("1234").oauth2Type(OAuth2Type.KAKAO).build();

        // when
        userRepository.save(user);

        // then
        Optional<User> result = userRepository.findById(user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getCreatedDateTime()).isNotNull();
        assertThat(result.get().getModifiedDateTime()).isNotNull();

        LocalDateTime now = LocalDateTime.now();
        assertThat(result.get().getCreatedDateTime()).isBeforeOrEqualTo(now);
        assertThat(result.get().getModifiedDateTime()).isBeforeOrEqualTo(now);

    }
}
