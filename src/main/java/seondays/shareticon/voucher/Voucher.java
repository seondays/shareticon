package seondays.shareticon.voucher;

import jakarta.persistence.Column;
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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import seondays.shareticon.group.Group;
import seondays.shareticon.user.User;
import seondays.shareticon.utils.BaseEntity;

@Entity
@Table(name = "voucher")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Voucher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate expiration;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String image;
    @Enumerated(value = EnumType.STRING)
    private VoucherStatus status;
    @Column(name = "is_deleted")
    private boolean isDeleted;

    public static Voucher createNewVoucher(User user, Group group, String name, String imageKey,
            LocalDate expiration) {
        return Voucher.builder()
                .user(user)
                .group(group)
                .name(name)
                .image(imageKey)
                .expiration(expiration)
                .isDeleted(false)
                .status(VoucherStatus.AVAILABLE)
                .build();
    }

    public void delete() {
        isDeleted = true;
    }

    public void changeStatus() {
        status.validateVoucherStatusExpired();
        if (status.equals(VoucherStatus.AVAILABLE)) {
            status = VoucherStatus.USED;
        } else if (status.equals(VoucherStatus.USED)) {
            status = VoucherStatus.AVAILABLE;
        }
    }

    public void changeStatusExpired() {
        status = VoucherStatus.EXPIRED;
    }
}
