package seondays.shareticon.userGroup;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    List<UserGroup> findAllByUserId(Long userId);
}
