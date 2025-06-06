package seondays.shareticon.userGroup;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import seondays.shareticon.group.JoinStatus;
import seondays.shareticon.group.dto.GroupListResponse;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    boolean existsByUserIdAndGroupId(Long userId, Long groupId);

    List<UserGroup> findAllByUserId(Long userId);

    Optional<UserGroup> findByUserIdAndGroupId(Long userId, Long groupId);

    @Query("""
            select ug
              from UserGroup ug
              JOIN FETCH ug.user
              where ug.group.leaderUser.id = :leaderId
                and ug.joinStatus = :status
                """)
    List<UserGroup> findByLeaderAndJoinStatus(Long leaderId, JoinStatus status);

    @Query("""
            SELECT new seondays.shareticon.group.dto.GroupListResponse(
                g.id,
                ug.groupTitleAlias,
                SIZE(g.userGroups)
            )
            FROM UserGroup ug
            JOIN ug.group g
            WHERE ug.user.id = :userId
            """)
    List<GroupListResponse> findGroupsWithMemberCountByUserId(Long userId);
}
