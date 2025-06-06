package seondays.shareticon.api.voucher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seondays.shareticon.api.config.RepositoryTestSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherRepository;
import seondays.shareticon.voucher.VoucherStatus;

class VoucherRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Test
    @DisplayName("전체 쿠폰의 첫번째 페이지를 조회한다")
    void getAllVoucherWithFirstPage() {
        //given
        Group group = Group.builder()
                .build();
        groupRepository.save(group);

        Voucher voucher1 = createVoucher(group, VoucherStatus.AVAILABLE);
        Voucher voucher2 = createVoucher(group, VoucherStatus.USED);
        Voucher voucher3 = createVoucher(group, VoucherStatus.AVAILABLE);
        Voucher voucher4 = createVoucher(group, VoucherStatus.AVAILABLE);
        Voucher voucher5 = createVoucher(group, VoucherStatus.USED);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3, voucher4, voucher5));

        Pageable pageable = PageRequest.of(0, 2);

        List<VoucherStatus> voucherStatuses = VoucherStatus.forDisplayVoucherStatus();

        //when
        Slice<Voucher> firstPage = voucherRepository.findAllPageWithCursorByDesc(group.getId(),
                voucherStatuses, null, pageable);

        //then
        assertThat(firstPage.getContent()).hasSize(pageable.getPageSize());
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.getContent())
                .allMatch(v -> voucherStatuses.contains(v.getStatus()));
        assertThat(firstPage.getContent()).isSortedAccordingTo(
                Comparator.comparing(Voucher::getId).reversed());
        assertThat(firstPage.getContent()).containsExactly(voucher5, voucher4);
    }

    @Test
    @DisplayName("전체 쿠폰의 두번째 페이지를 조회한다")
    void getAllVoucherWithCursor() {
        //given
        Group group = Group.builder()
                .build();
        groupRepository.save(group);

        Voucher voucher1 = createVoucher(group, VoucherStatus.AVAILABLE);
        Voucher voucher2 = createVoucher(group, VoucherStatus.USED);
        Voucher voucher3 = createVoucher(group, VoucherStatus.AVAILABLE);
        Voucher voucher4 = createVoucher(group, VoucherStatus.AVAILABLE);
        Voucher voucher5 = createVoucher(group, VoucherStatus.USED);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3, voucher4, voucher5));

        Pageable pageable = PageRequest.of(0, 2);

        List<VoucherStatus> voucherStatuses = VoucherStatus.forDisplayVoucherStatus();

        Slice<Voucher> firstPage = voucherRepository.findAllPageWithCursorByDesc(
                group.getId(), voucherStatuses, null, pageable);

        Long cursor = firstPage.getContent().get(firstPage.getContent().size() - 1).getId();

        //when
        Slice<Voucher> secondPage = voucherRepository.findAllPageWithCursorByDesc(group.getId(),
                voucherStatuses, cursor, pageable);

        //then
        assertThat(secondPage.getContent()).hasSize(pageable.getPageSize());
        assertThat(secondPage.hasNext()).isTrue();
        assertThat(secondPage.getContent())
                .allMatch(v -> voucherStatuses.contains(v.getStatus()));
        assertThat(secondPage.getContent()).isSortedAccordingTo(
                Comparator.comparing(Voucher::getId).reversed());
        assertThat(secondPage.getContent()).containsExactly(voucher3, voucher2);
    }

    @Test
    @DisplayName("전체 쿠폰의 마지막 페이지를 조회한다")
    void getAllVoucherWithLastPage() {
        //given
        Group group = Group.builder()
                .build();
        groupRepository.save(group);

        Voucher voucher1 = createVoucher(group, VoucherStatus.AVAILABLE);
        Voucher voucher2 = createVoucher(group, VoucherStatus.USED);
        Voucher voucher3 = createVoucher(group, VoucherStatus.AVAILABLE);
        Voucher voucher4 = createVoucher(group, VoucherStatus.AVAILABLE);
        Voucher voucher5 = createVoucher(group, VoucherStatus.USED);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3, voucher4, voucher5));

        Pageable pageable = PageRequest.of(0, 2);

        List<VoucherStatus> voucherStatuses = VoucherStatus.forDisplayVoucherStatus();

        Slice<Voucher> firstPage = voucherRepository.findAllPageWithCursorByDesc(
                group.getId(), voucherStatuses, null, pageable);
        Long firstCursor = firstPage.getContent().get(firstPage.getContent().size() - 1).getId();

        Slice<Voucher> secondPage = voucherRepository.findAllPageWithCursorByDesc(
                group.getId(), voucherStatuses, firstCursor, pageable);
        Long secondCursor = secondPage.getContent().get(secondPage.getContent().size() - 1).getId();

        //when
        Slice<Voucher> lastPage = voucherRepository.findAllPageWithCursorByDesc(
                group.getId(), voucherStatuses, secondCursor, pageable);

        //then
        assertThat(lastPage.getContent()).hasSize(1);
        assertThat(lastPage.hasNext()).isFalse();
        assertThat(lastPage.getContent())
                .allMatch(v -> voucherStatuses.contains(v.getStatus()));
        assertThat(lastPage.getContent()).isSortedAccordingTo(
                Comparator.comparing(Voucher::getId).reversed());
    }

    @Test
    @DisplayName("사용가능, 사용완료 상태인 쿠폰만 조회된다")
    void getVoucherStatusUsedAndAvailable() {
        //given
        Group group = Group.builder()
                .build();
        groupRepository.save(group);

        Voucher voucher1 = createVoucher(group, VoucherStatus.AVAILABLE);
        Voucher voucher2 = createVoucher(group, VoucherStatus.USED);
        Voucher voucher3 = createVoucher(group, VoucherStatus.EXPIRED);
        Voucher voucher4 = createVoucher(group, VoucherStatus.AVAILABLE);
        Voucher voucher5 = createVoucher(group, VoucherStatus.EXPIRED);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3, voucher4, voucher5));

        List<VoucherStatus> voucherStatuses = VoucherStatus.forDisplayVoucherStatus();

        Pageable pageable = PageRequest.of(0, 3);

        //when
        Slice<Voucher> result = voucherRepository.findAllPageWithCursorByDesc(
                group.getId(), voucherStatuses, null, pageable);

        //then
        assertThat(result.getContent()).extracting("status")
                .containsExactlyInAnyOrder(
                        VoucherStatus.AVAILABLE, VoucherStatus.USED, VoucherStatus.AVAILABLE);
    }

    @Test
    @DisplayName("쿠폰이 존재하지 않을 때 조회하는 경우 예외 없이 빈 목록을 반환한다")
    void getEmptyVoucher() {
        //given
        Group group = Group.builder()
                .build();
        groupRepository.save(group);

        Pageable pageable = PageRequest.of(0, 2);

        List<VoucherStatus> voucherStatuses = VoucherStatus.forDisplayVoucherStatus();

        //when
        Slice<Voucher> resultPage = voucherRepository.findAllPageWithCursorByDesc(
                group.getId(), voucherStatuses, null, pageable);

        //then
        assertThat(resultPage.getContent()).isEmpty();
        assertThat(resultPage.hasNext()).isFalse();
    }


    private static Voucher createVoucher(Group group, VoucherStatus status) {
        return Voucher.builder()
                .group(group)
                .status(status)
                .build();
    }
}