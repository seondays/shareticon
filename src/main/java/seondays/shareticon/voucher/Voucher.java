package seondays.shareticon.voucher;

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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import seondays.shareticon.group.Group;
import seondays.shareticon.user.User;

@Entity
@Table(name = "voucher")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    private String image;
    @Enumerated(value = EnumType.STRING)
    private VoucherStatus status;

    public static Voucher createAvailableStatus(User user, Group group) {
        return Voucher.builder()
                .user(user)
                .group(group)
                .status(VoucherStatus.AVAILABLE)
                .build();
    }

    public void saveImage(String image) {
        this.image = image;
    }

    public void changeStatus(VoucherStatus status) {
        this.status = status;
    }
}
