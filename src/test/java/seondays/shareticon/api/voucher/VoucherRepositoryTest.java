package seondays.shareticon.api.voucher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import seondays.shareticon.api.config.RepositoryTestSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherFilterCondition;
import seondays.shareticon.voucher.VoucherRepository;
import seondays.shareticon.voucher.VoucherStatus;
import seondays.shareticon.voucher.dto.VoucherWithWishListResponse;

class VoucherRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    protected Clock clock;

    private Instant testSystemTimeInstant;

    @BeforeEach
    void setUp() {
        testSystemTimeInstant = Instant.parse("2024-12-01T00:00:00Z");
        when(clock.instant()).thenReturn(testSystemTimeInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
    }

    @Test
    @DisplayName("전체 쿠폰의 첫번째 페이지를 조회한다")
    void getAllVoucherWithFirstPage() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        LocalDate expiration = LocalDate.of(2024, 12, 5);
        Voucher voucher1 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiration);
        Voucher voucher2 = createVoucherWithExpiration(group, user, VoucherStatus.USED, expiration);
        Voucher voucher3 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiration);
        Voucher voucher4 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiration);
        Voucher voucher5 = createVoucherWithExpiration(group, user, VoucherStatus.USED, expiration);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3, voucher4, voucher5));

        Pageable pageable = PageRequest.ofSize(2);

        List<VoucherStatus> voucherStatuses = VoucherStatus.forDisplayVoucherStatus();

        LocalDate searchStart = null;
        LocalDate searchEnd = null;
        List<VoucherStatus> status = null;
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                status, searchStart, searchEnd);

        Slice<VoucherWithWishListResponse> firstPage = voucherRepository.searchVoucher(
                user.getId(), group.getId(), voucherFilterCondition, null, pageable);

        //then
        assertThat(firstPage.getContent()).hasSize(pageable.getPageSize());
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.getContent())
                .allMatch(v -> voucherStatuses.contains(v.status()));
        assertThat(firstPage.getContent()).isSortedAccordingTo(
                Comparator.comparing(VoucherWithWishListResponse::id).reversed());
        assertThat(firstPage.getContent()).extracting("id")
                .containsExactly(voucher5.getId(), voucher4.getId());
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

        LocalDate expiration = LocalDate.of(2024, 12, 5);
        Voucher voucher1 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiration);
        Voucher voucher2 = createVoucherWithExpiration(group, user, VoucherStatus.USED, expiration);
        Voucher voucher3 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiration);
        Voucher voucher4 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiration);
        Voucher voucher5 = createVoucherWithExpiration(group, user, VoucherStatus.USED, expiration);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3, voucher4, voucher5));

        Pageable pageable = PageRequest.ofSize(2);

        List<VoucherStatus> voucherStatuses = VoucherStatus.forDisplayVoucherStatus();

        LocalDate searchStart = null;
        LocalDate searchEnd = null;
        List<VoucherStatus> status = null;
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                status, searchStart, searchEnd);

        Slice<VoucherWithWishListResponse> firstPage = voucherRepository.searchVoucher(
                user.getId(), group.getId(), voucherFilterCondition, null, pageable);

        Long cursor = firstPage.getContent().get(firstPage.getContent().size() - 1).id();

        //when
        Slice<VoucherWithWishListResponse> secondPage = voucherRepository.searchVoucher(
                user.getId(), group.getId(), voucherFilterCondition, cursor, pageable);

        //then
        assertThat(secondPage.getContent()).hasSize(pageable.getPageSize());
        assertThat(secondPage.hasNext()).isTrue();
        assertThat(secondPage.getContent())
                .allMatch(v -> voucherStatuses.contains(v.status()));
        assertThat(secondPage.getContent()).isSortedAccordingTo(
                Comparator.comparing(VoucherWithWishListResponse::id).reversed());
            assertThat(secondPage.getContent()).extracting("id")
                    .containsExactly(voucher3.getId(), voucher2.getId());
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

        LocalDate expiration = LocalDate.of(2024, 12, 5);
        Voucher voucher1 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiration);
        Voucher voucher2 = createVoucherWithExpiration(group, user, VoucherStatus.USED, expiration);
        Voucher voucher3 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiration);
        Voucher voucher4 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiration);
        Voucher voucher5 = createVoucherWithExpiration(group, user, VoucherStatus.USED, expiration);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3, voucher4, voucher5));

        Pageable pageable = PageRequest.ofSize(2);

        List<VoucherStatus> voucherStatuses = VoucherStatus.forDisplayVoucherStatus();

        LocalDate searchStart = null;
        LocalDate searchEnd = null;
        List<VoucherStatus> status = null;
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                status, searchStart, searchEnd);

        Slice<VoucherWithWishListResponse> firstPage = voucherRepository.searchVoucher(
                user.getId(), group.getId(), voucherFilterCondition, null, pageable);
        Long firstCursor = firstPage.getContent().get(firstPage.getContent().size() - 1).id();

        Slice<VoucherWithWishListResponse> secondPage = voucherRepository.searchVoucher(
                user.getId(), group.getId(), voucherFilterCondition, firstCursor, pageable);
        Long secondCursor = secondPage.getContent().get(secondPage.getContent().size() - 1).id();

        //when
        Slice<VoucherWithWishListResponse> lastPage = voucherRepository.searchVoucher(
                user.getId(), group.getId(), voucherFilterCondition, secondCursor, pageable);

        //then
        assertThat(lastPage.getContent()).hasSize(1);
        assertThat(lastPage.hasNext()).isFalse();
        assertThat(lastPage.getContent())
                .allMatch(v -> voucherStatuses.contains(v.status()));
        assertThat(lastPage.getContent()).isSortedAccordingTo(
                Comparator.comparing(VoucherWithWishListResponse::id).reversed());
    }

    @Test
    @DisplayName("상태 조건을 만족하는 쿠폰만 조회된다")
    void getVoucherStatusCondition() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        LocalDate expiration = LocalDate.of(2024, 12, 5);
        Voucher voucher1 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiration);
        Voucher voucher2 = createVoucherWithExpiration(group, user, VoucherStatus.USED, expiration);
        Voucher voucher3 = createVoucherWithExpiration(group, user, VoucherStatus.EXPIRED,
                expiration);
        Voucher voucher4 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiration);
        Voucher voucher5 = createVoucherWithExpiration(group, user, VoucherStatus.USED, expiration);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3, voucher4, voucher5));

        Pageable pageable = PageRequest.ofSize(5);

        LocalDate searchStart = null;
        LocalDate searchEnd = null;
        List<VoucherStatus> status = List.of(VoucherStatus.AVAILABLE);
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                status, searchStart, searchEnd);

        //when
        Slice<VoucherWithWishListResponse> result = voucherRepository.searchVoucher(
                user.getId(), group.getId(), voucherFilterCondition, null, pageable);

        //then
        assertThat(result.getContent()).extracting("status")
                .contains(VoucherStatus.AVAILABLE, VoucherStatus.AVAILABLE);
    }

    @Test
    @DisplayName("만료 기간 조건을 만족하는 쿠폰만 조회된다")
    void getVoucherExpiredCondition() {
        //given
        User user = User.builder().build();
        userRepository.save(user);

        String inviteCode = "ABC";
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        LocalDate expiredConditionDate = LocalDate.of(2024, 11, 5);
        LocalDate notExpiredConditionDate = LocalDate.of(2024, 12, 5);
        Voucher voucher1 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                expiredConditionDate);
        Voucher voucher2 = createVoucherWithExpiration(group, user, VoucherStatus.USED,
                notExpiredConditionDate);
        Voucher voucher3 = createVoucherWithExpiration(group, user, VoucherStatus.EXPIRED,
                expiredConditionDate);
        Voucher voucher4 = createVoucherWithExpiration(group, user, VoucherStatus.AVAILABLE,
                notExpiredConditionDate);
        Voucher voucher5 = createVoucherWithExpiration(group, user, VoucherStatus.USED,
                notExpiredConditionDate);
        voucherRepository.saveAll(List.of(voucher1, voucher2, voucher3, voucher4, voucher5));

        Pageable pageable = PageRequest.ofSize(5);

        LocalDate searchStart = LocalDate.of(2024,12,1);
        LocalDate searchEnd = LocalDate.of(2024,12,10);
        List<VoucherStatus> status = null;
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                status, searchStart, searchEnd);

        //when
        Slice<VoucherWithWishListResponse> result = voucherRepository.searchVoucher(
                user.getId(), group.getId(), voucherFilterCondition, null, pageable);

        //then
        assertThat(result.getContent()).extracting("id")
                .containsExactlyInAnyOrder(voucher2.getId(), voucher4.getId(), voucher5.getId());

    }

    @Test
    @DisplayName("쿠폰이 존재하지 않을 때 조회하는 경우 예외 없이 빈 목록을 반환한다")
    void getEmptyVoucher() {
        //given
        String inviteCode = "ABC";
        Long userId = 1L;
        Group group = createTestGroup(inviteCode);
        groupRepository.save(group);

        Pageable pageable = PageRequest.ofSize(2);

        LocalDate searchStart = null;
        LocalDate searchEnd = null;
        List<VoucherStatus> status = List.of(VoucherStatus.AVAILABLE);
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                status, searchStart, searchEnd);

        //when
        Slice<VoucherWithWishListResponse> result = voucherRepository.searchVoucher(
                userId, group.getId(), voucherFilterCondition, null, pageable);

        //then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.hasNext()).isFalse();
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

        Pageable pageable = PageRequest.ofSize(2);

        LocalDate searchStart = null;
        LocalDate searchEnd = null;
        List<VoucherStatus> status = List.of(VoucherStatus.AVAILABLE);
        VoucherFilterCondition voucherFilterCondition = VoucherFilterCondition.of(
                status, searchStart, searchEnd);

        //when
        Slice<VoucherWithWishListResponse> result = voucherRepository.searchVoucher(
                user.getId(), group.getId(), voucherFilterCondition, null, pageable);

        //then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.hasNext()).isFalse();

    }

    private static Voucher createVoucherWithExpiration(Group group, User user, VoucherStatus status,
            LocalDate expiration) {
        return Voucher.builder()
                .user(user)
                .group(group)
                .isDeleted(false)
                .status(status)
                .expiration(expiration)
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