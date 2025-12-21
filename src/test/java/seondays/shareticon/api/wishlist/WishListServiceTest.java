package seondays.shareticon.api.wishlist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.api.config.IntegrationTestSupport;
import seondays.shareticon.exception.business.InvalidAccessException;
import seondays.shareticon.exception.business.UserNotFoundException;
import seondays.shareticon.exception.business.VoucherNotFoundException;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.group.JoinStatus;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.utils.SliceResponse;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherRepository;
import seondays.shareticon.voucher.VoucherStatus;
import seondays.shareticon.wishlist.WishList;
import seondays.shareticon.wishlist.WishListRepository;
import seondays.shareticon.wishlist.WishListService;
import seondays.shareticon.wishlist.dto.WishListResponse;

class WishListServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private WishListRepository wishListRepository;

    @Autowired
    private WishListService wishListService;

    private Instant testSystemTimeInstant;

    @BeforeEach
    void setUp() {
        testSystemTimeInstant = Instant.parse("2024-12-01T00:00:00Z");
        when(clock.instant()).thenReturn(testSystemTimeInstant);
        when(clock.getZone()).thenReturn(ZoneId.of("Asia/Seoul"));
    }

    @AfterEach
    void tearDown() {
        userGroupRepository.deleteAllInBatch();
        wishListRepository.deleteAllInBatch();
        voucherRepository.deleteAllInBatch();
        groupRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("쿠폰 찜 내역이 존재하지 않을 경우 정상적으로 생성 및 활성화된다")
    void toggleWithNewWishlist() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        linkUserGroup(user, group);
        Voucher voucher = createNewVoucher(user, group);

        //when
        wishListService.toggleWishList(user.getId(), group.getId(), voucher.getId());

        //then
        WishList result = wishListRepository.findByUserIdAndVoucherId(user.getId(),
                voucher.getId()).orElseThrow();

        assertThat(result).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo(user.getId());
        assertThat(result.getVoucher().getId()).isEqualTo(voucher.getId());
        assertThat(result.isActive()).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("wishListToggleScenarios")
    @DisplayName("쿠폰 찜 내역이 존재하는 경우 현재 찜 상태를 반대로 토글한다")
    void toggleWishlist(String displayName, boolean active) {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        linkUserGroup(user, group);
        Voucher voucher = createNewVoucher(user, group);

        WishList wishList = WishList.builder()
                .isActive(!active)
                .voucher(voucher)
                .user(user)
                .group(group)
                .build();
        wishListRepository.save(wishList);

        //when
        wishListService.toggleWishList(user.getId(), group.getId(), voucher.getId());

        //then
        WishList result = wishListRepository.findByUserIdAndVoucherId(user.getId(),
                voucher.getId()).orElseThrow();

        assertThat(result).isNotNull();
        assertThat(result.isActive()).isEqualTo(active);

    }

    static Stream<Arguments> wishListToggleScenarios() {
        return Stream.of(
                Arguments.of("쿠폰 찜 상태가 비활성화된 경우 토글을 통해 활성 상태로 변경한다",
                        true),
                Arguments.of("쿠폰 찜 상태가 활성화된 경우 토글을 통해 비활성 상태로 변경한다",
                        false
                )
        );
    }

    @Test
    @DisplayName("쿠폰 찜을 요청한 유저가 존재하지 않는 경우 찜을 할 수 없다")
    void toggleWishListWithUserNotExist() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        linkUserGroup(user, group);
        Voucher voucher = createNewVoucher(user, group);

        //when //then
        assertThatThrownBy(() -> wishListService.toggleWishList(100L, group.getId(), voucher.getId()))
                .isInstanceOf(UserNotFoundException.class);

    }

    @Test
    @DisplayName("쿠폰 찜 대상 쿠폰이 존재하지 않는 경우 찜을 할 수 없다")
    void toggleWishListWithVoucherNotExist() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        linkUserGroup(user, group);

        //when //then
        assertThatThrownBy(() -> wishListService.toggleWishList(user.getId(), group.getId(), 100L))
                .isInstanceOf(VoucherNotFoundException.class);

    }

    @Test
    @DisplayName("쿠폰 찜을 요청한 유저가 대상 쿠폰에 접근할 수 없는 경우 찜을 할 수 없다")
    void toggleWishListWithUserGroupNotExist() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        Voucher voucher = createNewVoucher(user, group);

        //when //then
        assertThatThrownBy(() -> wishListService.toggleWishList(user.getId(), group.getId(), voucher.getId()))
                .isInstanceOf(InvalidAccessException.class);

    }

    @Test
    @Transactional
    @DisplayName("유저의 찜 목록을 모두 조회하는 경우 전체 결과를 담은 slice를 반환한다")
    void getAllWishList() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        linkUserGroup(user, group);
        Voucher voucher1 = createNewVoucher(user, group);
        Voucher voucher2 = createNewVoucher(user, group);

        createWishList(user, voucher1, group);
        createWishList(user, voucher2, group);

        //when
        SliceResponse<WishListResponse> result = wishListService.getAllWishList(user.getId(),
                null, 5);

        //then
        assertThat(result.getSize()).isEqualTo(5);
        assertThat(result.hasNext()).isFalse();

        List<WishListResponse> content = result.getContent();
        assertThat(content).isNotEmpty();
        assertThat(content.size()).isEqualTo(2);
        assertThat(content).extracting("voucherId").contains(voucher1.getId(), voucher2.getId());

    }

    @Test
    @DisplayName("존재하지 않는 유저는 찜 목록을 조회할 수 없다.")
    void getAllWishListWithUserNotExist() {
        //when // then
        assertThatThrownBy(() -> wishListService.getAllWishList(1L, null, 5))
                .isInstanceOf(UserNotFoundException.class);

    }

    @Test
    @DisplayName("찜 내역이 존재하지 않는 경우, 조회 시 Slice의 content는 비어 있다")
    void getAllWishListWithNotExist() {
        //given
        User user = createNewUser();

        //when
        SliceResponse<WishListResponse> result = wishListService.getAllWishList(user.getId(),
                null, 5);

        //then
        assertThat(result.getContent()).hasSize(0);

    }

    private User createNewUser() {
        User user = User.builder().build();
        return userRepository.save(user);
    }

    private Group createNewGroup(User user) {
        Group group = Group.createNewGroup(user, "TestCode", "테스트 그룹");
        return groupRepository.save(group);
    }

    private Voucher createNewVoucher(User user, Group group) {
        Voucher voucher = Voucher.createNewVoucher(user, group, "쿠폰", "imagekey",
                LocalDate.of(2020, 1, 1));
        return voucherRepository.save(voucher);
    }

    private WishList createWishList(User user, Voucher voucher, Group group) {
        return wishListRepository.save(
                WishList.builder().isActive(true).voucher(voucher).user(user).group(group).build());
    }

    private void linkUserGroup(User user, Group group) {
        UserGroup userGroup = UserGroup.builder().group(group).user(user)
                .joinStatus(JoinStatus.JOINED).build();
        userGroupRepository.save(userGroup);
    }

    private Voucher createVoucherWithStatus(User user, Group group, VoucherStatus status) {
        Voucher voucher = Voucher.builder()
                .user(user)
                .group(group)
                .isDeleted(false)
                .image("imagekey")
                .status(status)
                .build();
        return voucherRepository.save(voucher);
    }

}