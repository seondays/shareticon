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
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherRepository;
import seondays.shareticon.voucher.VoucherStatus;

class VoucherRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("전체 쿠폰의 첫번째 페이지를 조회한다")
    void getAllVoucherWithFirstPage() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        Voucher voucher1 = createVoucher(group, user, VoucherStatus.AVAILABLE);
        Voucher voucher2 = createVoucher(group, user, VoucherStatus.USED);
        Voucher voucher3 = createVoucher(group, user, VoucherStatus.AVAILABLE);
        Voucher voucher4 = createVoucher(group, user, VoucherStatus.AVAILABLE);
        Voucher voucher5 = createVoucher(group, user, VoucherStatus.USED);
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
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        Voucher voucher1 = createVoucher(group, user, VoucherStatus.AVAILABLE);
        Voucher voucher2 = createVoucher(group, user, VoucherStatus.USED);
        Voucher voucher3 = createVoucher(group, user, VoucherStatus.AVAILABLE);
        Voucher voucher4 = createVoucher(group, user, VoucherStatus.AVAILABLE);
        Voucher voucher5 = createVoucher(group, user, VoucherStatus.USED);
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
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        Voucher voucher1 = createVoucher(group, user, VoucherStatus.AVAILABLE);
        Voucher voucher2 = createVoucher(group, user, VoucherStatus.USED);
        Voucher voucher3 = createVoucher(group, user, VoucherStatus.AVAILABLE);
        Voucher voucher4 = createVoucher(group, user, VoucherStatus.AVAILABLE);
        Voucher voucher5 = createVoucher(group, user, VoucherStatus.USED);
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
    @DisplayName("사용가능, 사용완료, 사용만료 상태인 쿠폰만 조회된다")
    void getVoucherStatusUsedAndAvailable() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        Voucher voucher1 = createVoucher(group, user, VoucherStatus.AVAILABLE);
        Voucher voucher2 = createVoucher(group, user, VoucherStatus.USED);
        Voucher voucher3 = createVoucher(group, user, VoucherStatus.EXPIRED);
        Voucher voucher4 = createVoucher(group, user, VoucherStatus.AVAILABLE);
        Voucher voucher5 = createVoucher(group, user, VoucherStatus.EXPIRED);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3, voucher4, voucher5));

        List<VoucherStatus> voucherStatuses = VoucherStatus.forDisplayVoucherStatus();

        Pageable pageable = PageRequest.of(0, 5);

        //when
        Slice<Voucher> result = voucherRepository.findAllPageWithCursorByDesc(
                group.getId(), voucherStatuses, null, pageable);

        //then
        assertThat(result.getContent()).extracting("status")
                .containsExactlyInAnyOrder(
                        VoucherStatus.AVAILABLE, VoucherStatus.USED, VoucherStatus.EXPIRED,
                        VoucherStatus.AVAILABLE, VoucherStatus.EXPIRED);
    }

    @Test
    @DisplayName("쿠폰이 존재하지 않을 때 조회하는 경우 예외 없이 빈 목록을 반환한다")
    void getEmptyVoucher() {
        //given
        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
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

    @Test
    @DisplayName("특정 유저가 등록한 모든 쿠폰의 개수를 조회한다")
    void getAllVoucherByUser() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        Voucher voucher1 = createVoucherWithUser(group, VoucherStatus.AVAILABLE, user);
        Voucher voucher2 = createVoucherWithUser(group, VoucherStatus.USED, user);
        Voucher voucher3 = createVoucherWithUser(group, VoucherStatus.EXPIRED, user);
        Voucher voucher4 = createVoucherWithUser(group, VoucherStatus.AVAILABLE, user);
        Voucher voucher5 = createVoucherWithUser(group, VoucherStatus.EXPIRED, user);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3, voucher4, voucher5));

        //when
        Long result = voucherRepository.countByUserIdAndIsDeletedFalse(user.getId());

        //then
        assertThat(result).isEqualTo(5);

    }

    @Test
    @DisplayName("특정 유저가 등록한 모든 쿠폰의 개수가 0개여도 정상적으로 조회된다")
    void getAllVoucherByUserZero() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        //when
        Long result = voucherRepository.countByUserIdAndIsDeletedFalse(user.getId());

        //then
        assertThat(result).isZero();

    }

    @Test
    @DisplayName("삭제 상태인 쿠폰은 조회에 포함되지 않는다")
    void getAllVoucherWithNotDeleted() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        Voucher voucher = Voucher.builder().user(user).group(group).isDeleted(true)
                .status(VoucherStatus.AVAILABLE).build();
        voucherRepository.save(voucher);

        List<VoucherStatus> voucherStatuses = VoucherStatus.forDisplayVoucherStatus();

        Pageable pageable = PageRequest.of(0, 2);

        //when
        Slice<Voucher> resultPage = voucherRepository.findAllPageWithCursorByDesc(
                group.getId(), voucherStatuses, null, pageable);

        //then
        assertThat(resultPage.getContent()).isEmpty();
        assertThat(resultPage.hasNext()).isFalse();

    }

    private static Voucher createVoucher(Group group, User user, VoucherStatus status) {
        return Voucher.builder()
                .user(user)
                .group(group)
                .isDeleted(false)
                .status(status)
                .build();
    }

    private static Voucher createVoucherWithUser(Group group, VoucherStatus status, User user) {
        return Voucher.builder()
                .group(group)
                .user(user)
                .status(status)
                .build();
    }

    public Group createTestGroup(String inviteCode) {
        User user = User.builder().build();
        userRepository.save(user);
        return Group.builder().inviteCode(inviteCode).leaderUser(user).build();
    }
}