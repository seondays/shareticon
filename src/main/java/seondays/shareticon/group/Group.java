package seondays.shareticon.group;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seondays.shareticon.group.dto.PendingMemberResponse;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.utils.BaseEntity;
import seondays.shareticon.user.User;

@Entity
@Table(name = "voucher_group")
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Group extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_user_id", nullable = false)
    private User leaderUser;
    @Column(unique = true)
    private String inviteCode;
    private String title;
    @Builder.Default
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<UserGroup> userGroups = new ArrayList<>();

    public List<PendingMemberResponse> getPendingMemberResponses() {
        return this.userGroups.stream()
                .filter(userGroup -> userGroup.getJoinStatus() == JoinStatus.PENDING)
                .map(userGroup -> PendingMemberResponse.from(userGroup.getUser()))
                .toList();
    }

    public String getGroupAlias(Long userId) {
        return this.userGroups.stream()
                .filter(userGroup -> userGroup.getUser().getId().equals(userId))
                .map(UserGroup::getGroupTitleAlias)
                .findFirst()
                .orElse(title);
    }

    public static Group createNewGroup(User leaderUser, String inviteCode, String title) {
        return Group.builder()
                .leaderUser(leaderUser)
                .inviteCode(inviteCode)
                .title(title)
                .build();
    }

    public static Group WithUserGroup(Group group, List<UserGroup> userGroups) {
        group.userGroups = userGroups;
        return group;
    }
}
