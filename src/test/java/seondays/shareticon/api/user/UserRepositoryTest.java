package seondays.shareticon.api.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import seondays.shareticon.login.OAuth2Type;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;

@ActiveProfiles("test")
@DataJpaTest
public class UserRepositoryTest {

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
}
