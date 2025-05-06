package seondays.shareticon.userGroup;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import seondays.shareticon.group.JoinStatus;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    List<UserGroup> findAllByUserId(Long userId);

    Optional<UserGroup> findByUserIdAndGroupId(Long userId, Long groupId);
}
