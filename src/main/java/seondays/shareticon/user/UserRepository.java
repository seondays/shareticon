package seondays.shareticon.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seondays.shareticon.login.OAuth2Type;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauth2IdAndOauth2Type(String oauth2Id, OAuth2Type oauth2Type);
}
