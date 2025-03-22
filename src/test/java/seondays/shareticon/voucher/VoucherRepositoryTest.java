package seondays.shareticon.voucher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;

@ActiveProfiles("test")
@DataJpaTest
class VoucherRepositoryTest {

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
        Slice<Voucher> firstPage = voucherRepository.findAllPageWithCursor(group.getId(),
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

        Slice<Voucher> firstPage = voucherRepository.findAllPageWithCursor(
                group.getId(), voucherStatuses, null, pageable);

        Long cursor = firstPage.getContent().get(firstPage.getContent().size() - 1).getId();

        //when
        Slice<Voucher> secondPage = voucherRepository.findAllPageWithCursor(group.getId(),
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

        Slice<Voucher> firstPage = voucherRepository.findAllPageWithCursor(
                group.getId(), voucherStatuses, null, pageable);
        Long firstCursor = firstPage.getContent().get(firstPage.getContent().size() - 1).getId();

        Slice<Voucher> secondPage = voucherRepository.findAllPageWithCursor(
                group.getId(), voucherStatuses, firstCursor, pageable);
        Long secondCursor = secondPage.getContent().get(secondPage.getContent().size() - 1).getId();

        //when
        Slice<Voucher> lastPage = voucherRepository.findAllPageWithCursor(
                group.getId(), voucherStatuses, secondCursor, pageable);

        //then
        assertThat(lastPage.getContent()).hasSize(1);
        assertThat(lastPage.hasNext()).isFalse();
        assertThat(lastPage.getContent())
                .allMatch(v -> voucherStatuses.contains(v.getStatus()));
        assertThat(lastPage.getContent()).isSortedAccordingTo(
                Comparator.comparing(Voucher::getId).reversed());
    }



    private static Voucher createVoucher(Group group, VoucherStatus status) {
        return Voucher.builder()
                .group(group)
                .status(status)
                .build();
    }
}