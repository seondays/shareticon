package seondays.shareticon.group;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    // distinct를 통해 Group의 중복을 제거하고 엔티티를 올바르게 매핑하도록 명시적으로 지시한다
    @Query("""
            select distinct g
            from Group g
            join fetch g.userGroups ug
            join fetch ug.user
            where g.leaderUser.id = :leaderId
            """)
    List<Group> findAllByLeaderId(Long leaderId);
}
