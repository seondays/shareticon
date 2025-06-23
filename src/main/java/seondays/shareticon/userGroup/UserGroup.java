package seondays.shareticon.userGroup;

import static seondays.shareticon.group.JoinStatus.JOINED;
import static seondays.shareticon.group.JoinStatus.PENDING;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seondays.shareticon.group.ApprovalStatus;
import seondays.shareticon.utils.BaseEntity;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.JoinStatus;
import seondays.shareticon.user.User;

@Entity
@Table(name = "user_voucher_group")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Enumerated(EnumType.STRING)
    private JoinStatus joinStatus;

    private String groupTitleAlias;

    public static UserGroup createLeaderUserGroup(User leaderUser, Group group) {
        return UserGroup.builder()
                .group(group)
                .user(leaderUser)
                .groupTitleAlias(group.getTitle())
                .joinStatus(JOINED)
                .build();
    }

    public static UserGroup createNewPending(User user, Group group) {
        return UserGroup.builder()
                .group(group)
                .groupTitleAlias(group.getTitle())
                .user(user)
                .joinStatus(PENDING)
                .build();
    }

    public static UserGroup applyToJoin(User user, Group group,
            Optional<UserGroup> existingUserGroup) {
        return existingUserGroup.map(
                existing -> {
                    existing.changeJoinStatusToPending();
                    return existing;
                }).orElseGet(() -> UserGroup.createNewPending(user, group));
    }

    public void approvalJoinStatus(ApprovalStatus approvalStatus) {
        joinStatus.isWaitingAcceptJoinApply();

        if (ApprovalStatus.isApproved(approvalStatus)) {
            joinStatus = JoinStatus.JOINED;
        } else {
            joinStatus = JoinStatus.REJECTED;
        }
    }

    public void changeGroupTitleAlias(String alias) {
        this.groupTitleAlias = alias;
    }

    public void changeJoinStatusToPending() {
        joinStatus.validateAlreadyApplied();
        joinStatus = PENDING;
    }
}
