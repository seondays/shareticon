package seondays.shareticon.wishlist;

import jakarta.persistence.Entity;
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
import seondays.shareticon.group.Group;
import seondays.shareticon.user.User;
import seondays.shareticon.utils.BaseEntity;
import seondays.shareticon.voucher.Voucher;

@Table(name = "wishlist")
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WishList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    private boolean isActive;

    public static WishList toggleActive(User user, Voucher voucher, Group group,
            Optional<WishList> existingWishList) {
        return existingWishList.map(
                existing -> {
                    existing.isActive = !existing.isActive;
                    return existing;
                }).orElseGet(() -> WishList.of(user, voucher, group));
    }

    public static WishList of(User user, Voucher voucher, Group group) {
        return WishList.builder()
                .user(user)
                .voucher(voucher)
                .group(group)
                .isActive(true)
                .build();
    }
}
