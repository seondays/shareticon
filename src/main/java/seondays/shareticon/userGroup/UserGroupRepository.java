package seondays.shareticon.userGroup;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import seondays.shareticon.group.JoinStatus;
import seondays.shareticon.group.dto.GroupListResponse;

@Repository
public interface UserGroupRepository extends JpaRepository<UserGroup, Long> {

    boolean existsByUserIdAndGroupIdAndJoinStatus(Long userId, Long groupId, JoinStatus status);

    @Query("""
            SELECT COUNT(ug)
            FROM UserGroup ug
            WHERE ug.user.id = :userId AND ug.joinStatus = 'JOINED'""")
    Long countByUserIdAndJoined(Long userId);

    Optional<UserGroup> findByUserIdAndGroupId(Long userId, Long groupId);

    @Query("""
            SELECT ug
              FROM UserGroup ug
              JOIN FETCH ug.user
              WHERE ug.group.leaderUser.id = :leaderId
                AND ug.joinStatus = :status
                """)
    List<UserGroup> findByLeaderAndJoinStatus(Long leaderId, JoinStatus status);

    @Query("""
            SELECT new seondays.shareticon.group.dto.GroupListResponse(
                g.id,
                ug.groupTitleAlias,
                COUNT(ug2.id)
            )
            FROM UserGroup ug
            JOIN ug.group g
            LEFT JOIN UserGroup ug2 ON ug2.group.id = g.id AND ug2.joinStatus IN :status
            WHERE ug.user.id = :userId
            AND ug.joinStatus in :status
            GROUP BY g.id, ug.groupTitleAlias
            """)
    List<GroupListResponse> findGroupsWithMemberCountByUserIdAndStatus(Long userId,
            List<JoinStatus> status);
}
