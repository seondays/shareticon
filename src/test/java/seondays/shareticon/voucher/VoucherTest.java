package seondays.shareticon.voucher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import seondays.shareticon.exception.business.ExpiredVoucherException;
import seondays.shareticon.group.Group;
import seondays.shareticon.user.User;

class VoucherTest {

    @Test
    @DisplayName("AVAILABLE 상태의 쿠폰을 USED로 전이시키고 true를 반환한다")
    void markAsUsedTransitionsFromAvailable() {
        //given
        Voucher voucher = newVoucher(VoucherStatus.AVAILABLE);

        //when
        boolean changed = voucher.markAsUsed();

        //then
        assertThat(changed).isTrue();
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.USED);
    }

    @Test
    @DisplayName("이미 USED 상태인 쿠폰에 markAsUsed를 호출하면 상태를 유지하고 false를 반환한다")
    void markAsUsedIsNoOpWhenAlreadyUsed() {
        //given
        Voucher voucher = newVoucher(VoucherStatus.USED);

        //when
        boolean changed = voucher.markAsUsed();

        //then
        assertThat(changed).isFalse();
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.USED);
    }

    @Test
    @DisplayName("EXPIRED 상태인 쿠폰에 markAsUsed를 호출하면 예외가 발생한다")
    void markAsUsedThrowsWhenExpired() {
        //given
        Voucher voucher = newVoucher(VoucherStatus.EXPIRED);

        //when //then
        assertThatThrownBy(voucher::markAsUsed)
                .isInstanceOf(ExpiredVoucherException.class);
    }

    @Test
    @DisplayName("USED 상태의 쿠폰을 AVAILABLE로 전이시키고 true를 반환한다")
    void markAsAvailableTransitionsFromUsed() {
        //given
        Voucher voucher = newVoucher(VoucherStatus.USED);

        //when
        boolean changed = voucher.markAsAvailable();

        //then
        assertThat(changed).isTrue();
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.AVAILABLE);
    }

    @Test
    @DisplayName("이미 AVAILABLE 상태인 쿠폰에 markAsAvailable을 호출하면 상태를 유지하고 false를 반환한다")
    void markAsAvailableIsNoOpWhenAlreadyAvailable() {
        //given
        Voucher voucher = newVoucher(VoucherStatus.AVAILABLE);

        //when
        boolean changed = voucher.markAsAvailable();

        //then
        assertThat(changed).isFalse();
        assertThat(voucher.getStatus()).isEqualTo(VoucherStatus.AVAILABLE);
    }

    @Test
    @DisplayName("EXPIRED 상태인 쿠폰에 markAsAvailable을 호출하면 예외가 발생한다")
    void markAsAvailableThrowsWhenExpired() {
        //given
        Voucher voucher = newVoucher(VoucherStatus.EXPIRED);

        //when //then
        assertThatThrownBy(voucher::markAsAvailable)
                .isInstanceOf(ExpiredVoucherException.class);
    }

    private Voucher newVoucher(VoucherStatus status) {
        return Voucher.builder()
                .user(User.builder().build())
                .group(Group.builder().build())
                .name("voucher")
                .image("imageKey")
                .expiration(LocalDate.of(2025, 1, 1))
                .isDeleted(false)
                .status(status)
                .build();
    }
}
