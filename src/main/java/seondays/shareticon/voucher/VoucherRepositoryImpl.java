package seondays.shareticon.voucher;

import static seondays.shareticon.voucher.QVoucher.*;
import static seondays.shareticon.wishlist.QWishList.*;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import seondays.shareticon.voucher.dto.QVoucherWithWishListResponse;
import seondays.shareticon.voucher.dto.VoucherWithWishListResponse;

@RequiredArgsConstructor
public class VoucherRepositoryImpl implements VoucherQueryDslRepository {

    private final JPAQueryFactory jpaQueryFactory;
    private final Clock clock;

    @Override
    public Slice<VoucherWithWishListResponse> searchVoucher(Long userId, Long groupId,
            VoucherFilterCondition voucherFilterCondition, Long cursorId, Pageable pageable) {

        int pageSize = pageable.getPageSize();

        List<VoucherWithWishListResponse> result = jpaQueryFactory.select(
                        new QVoucherWithWishListResponse(
                                voucher,
                                wishList.isActive.isTrue()
                        ))
                .from(voucher)
                .leftJoin(wishList)
                .on(wishList.voucher.eq(voucher).and(wishList.user.id.eq(userId)))
                .where(
                        voucher.group.id.eq(groupId),
                        expirationBetween(voucherFilterCondition.startDay(),
                                voucherFilterCondition.endDay()),
                        voucherStatusIn(voucherFilterCondition.voucherStatuses()),
                        voucher.isDeleted.isFalse(),
                        cursorCondition(cursorId)
                )
                .orderBy(voucher.id.desc())
                .limit(pageSize + 1)
                .fetch();

        boolean hasNext = result.size() > pageSize;

        if (hasNext) {
            result.remove(pageSize);
        }

        return new SliceImpl<>(result, pageable, hasNext);
    }

    private BooleanExpression cursorCondition(Long cursorId) {
        return cursorId == null ? null : voucher.id.lt(cursorId);
    }

    private BooleanExpression expirationBetween(LocalDate startDay, LocalDate endDay) {
        LocalDate defaultStartDay = LocalDate.now(clock);
        LocalDate defaultEndDay = defaultStartDay.plusMonths(1)
                .with(TemporalAdjusters.lastDayOfMonth());

        LocalDate actualStartDay = startDay == null ? defaultStartDay : startDay;
        LocalDate actualEndDay = endDay == null ? defaultEndDay : endDay;

        return voucher.expiration.between(actualStartDay, actualEndDay);
    }

    private BooleanExpression voucherStatusIn(List<VoucherStatus> voucherStatuses) {
        List<VoucherStatus> targetStatuses = Optional.ofNullable(voucherStatuses)
                .filter(list -> !list.isEmpty())
                .orElse(VoucherStatus.forDisplayVoucherStatus());
        return voucher.status.in(targetStatuses);
    }
}
