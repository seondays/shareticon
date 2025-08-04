package seondays.shareticon.api.wishlist;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import seondays.shareticon.api.config.RepositoryTestSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.group.JoinStatus;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherRepository;
import seondays.shareticon.wishlist.WishList;
import seondays.shareticon.wishlist.WishListRepository;

public class WishListRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private WishListRepository wishListRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Test
    @DisplayName("전체 찜 내역의 첫번째 페이지를 조회한다")
    void getAllWishListSliceWithFirstPage() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        Voucher voucher1 = createVoucher(user, group);
        Voucher voucher2 = createVoucher(user, group);
        Voucher voucher3 = createVoucher(user, group);
        Voucher voucher4 = createVoucher(user, group);

        WishList wishList1 = createWishList(user, voucher1, group);
        WishList wishList2 = createWishList(user, voucher2, group);
        WishList wishList3 = createWishList(user, voucher3, group);
        WishList wishList4 = createWishList(user, voucher4, group);

        Pageable pageable = Pageable.ofSize(2);

        //when
        Slice<WishList> firstPage = wishListRepository.findAllByUserId(user.getId(), null,
                pageable);

        //then
        assertThat(firstPage.isFirst()).isTrue();
        assertThat(firstPage.hasPrevious()).isFalse();
        assertThat(firstPage.getContent()).hasSize(pageable.getPageSize());
        assertThat(firstPage.getContent()).isSortedAccordingTo(
                Comparator.comparing(WishList::getId).reversed());
        assertThat(firstPage.getContent()).containsExactly(wishList4, wishList3);

    }

    @Test
    @DisplayName("전체 찜 내역의 중간 페이지를 조회한다")
    void getAllWishListSliceWithMiddlePage() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        Voucher voucher1 = createVoucher(user, group);
        Voucher voucher2 = createVoucher(user, group);
        Voucher voucher3 = createVoucher(user, group);
        Voucher voucher4 = createVoucher(user, group);
        Voucher voucher5 = createVoucher(user, group);

        WishList wishList1 = createWishList(user, voucher1, group);
        WishList wishList2 = createWishList(user, voucher2, group);
        WishList wishList3 = createWishList(user, voucher3, group);
        WishList wishList4 = createWishList(user, voucher4, group);
        WishList wishList5 = createWishList(user, voucher5, group);

        Pageable pageable = Pageable.ofSize(2);

        Slice<WishList> firstPage = wishListRepository.findAllByUserId(user.getId(), null,
                pageable);
        Long cursor = firstPage.getContent().get(firstPage.getContent().size() - 1).getId();

        //when
        Slice<WishList> middlePage = wishListRepository.findAllByUserId(user.getId(), cursor,
                pageable);

        //then
        assertThat(middlePage.hasNext()).isTrue();
        assertThat(middlePage.getSize()).isEqualTo(pageable.getPageSize());
        assertThat(middlePage.getContent()).hasSize(pageable.getPageSize());
        assertThat(firstPage.getContent()).isSortedAccordingTo(
                Comparator.comparing(WishList::getId).reversed());
        assertThat(middlePage.getContent()).containsExactly(wishList3, wishList2);

    }

    @Test
    @DisplayName("전체 찜 내역의 마지막 페이지를 조회한다")
    void getAllWishListSliceWithLastPage() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        Voucher voucher1 = createVoucher(user, group);
        Voucher voucher2 = createVoucher(user, group);
        Voucher voucher3 = createVoucher(user, group);
        Voucher voucher4 = createVoucher(user, group);
        Voucher voucher5 = createVoucher(user, group);

        WishList wishList1 = createWishList(user, voucher1, group);
        WishList wishList2 = createWishList(user, voucher2, group);
        WishList wishList3 = createWishList(user, voucher3, group);
        WishList wishList4 = createWishList(user, voucher4, group);
        WishList wishList5 = createWishList(user, voucher5, group);

        Pageable pageable = Pageable.ofSize(2);

        Slice<WishList> firstPage = wishListRepository.findAllByUserId(user.getId(), null,
                pageable);
        Long cursor = firstPage.getContent().get(firstPage.getContent().size() - 1).getId();

        Slice<WishList> middlePage = wishListRepository.findAllByUserId(user.getId(), cursor,
                pageable);
        Long middleCursor = middlePage.getContent().get(middlePage.getContent().size() - 1).getId();

        //when
        Slice<WishList> lastPage = wishListRepository.findAllByUserId(user.getId(), middleCursor,
                pageable);

        //then
        assertThat(lastPage.isLast()).isTrue();
        assertThat(lastPage.hasNext()).isFalse();
        assertThat(lastPage.getSize()).isEqualTo(pageable.getPageSize());
        assertThat(lastPage.getContent()).hasSize(1);
        assertThat(lastPage.getContent()).containsExactly(wishList1);
    }

    @Test
    @DisplayName("조회 결과는 id 내림차순이다")
    void getAllWishListSliceWithDesc() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        Voucher voucher1 = createVoucher(user, group);
        Voucher voucher2 = createVoucher(user, group);
        Voucher voucher3 = createVoucher(user, group);
        Voucher voucher4 = createVoucher(user, group);
        Voucher voucher5 = createVoucher(user, group);

        WishList wishList1 = createWishList(user, voucher1, group);
        WishList wishList2 = createWishList(user, voucher2, group);
        WishList wishList3 = createWishList(user, voucher3, group);
        WishList wishList4 = createWishList(user, voucher4, group);
        WishList wishList5 = createWishList(user, voucher5, group);

        Pageable pageable = Pageable.ofSize(5);

        //when
        Slice<WishList> result = wishListRepository.findAllByUserId(user.getId(), null,
                pageable);

        //then
        assertThat(result.getContent()).isNotNull();
        assertThat(result.getContent()).isSortedAccordingTo(
                Comparator.comparing(WishList::getId).reversed());

    }

    @Test
    @DisplayName("활성화된 찜 목록만 조회해온다")
    void getAllWishListSliceWithActive() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        Voucher voucher1 = createVoucher(user, group);
        Voucher voucher2 = createVoucher(user, group);
        Voucher voucher3 = createVoucher(user, group);
        Voucher voucher4 = createVoucher(user, group);
        Voucher voucher5 = createVoucher(user, group);

        WishList wishList1 = createWishList(user, voucher1, group);
        WishList wishList2 = createWishListWithNotActive(user, voucher2, group);
        WishList wishList3 = createWishList(user, voucher3, group);
        WishList wishList4 = createWishListWithNotActive(user, voucher4, group);
        WishList wishList5 = createWishList(user, voucher5, group);

        Pageable pageable = Pageable.ofSize(5);

        //when
        Slice<WishList> result = wishListRepository.findAllByUserId(user.getId(), null,
                pageable);

        //then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).containsExactly(wishList5, wishList3, wishList1);

    }

    @Test
    @DisplayName("찜 내역이 없는 경우, 전체 찜 목록을 조회하면 비어있는 Slice가 조회된다")
    void getAllWishListSliceWithUserNotExist() {
        //given
        User user = createNewUser();

        Pageable pageable = Pageable.ofSize(5);

        //when
        Slice<WishList> result = wishListRepository.findAllByUserId(user.getId(), null,
                pageable);

        //then
        assertThat(result.getContent()).isEmpty();

    }

    @Test
    @DisplayName("유저의 특정 쿠폰에 대한 찜 내역을 조회한다")
    void getWishListWithUserAndVoucher() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        Voucher voucher = createVoucher(user, group);

        WishList wishList = createWishList(user, voucher, group);

        //when
        Optional<WishList> result = wishListRepository.findByUserIdAndVoucherId(user.getId(),
                voucher.getId());

        //then
        assertThat(result).isPresent();
        assertThat(result.get().getVoucher()).isEqualTo(voucher);
        assertThat(result.get().getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("특정 찜 내역이 존재하지 않는 경우 값이 반환되지 않는다")
    void getWishListWithNotExist() {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        Voucher voucher = createVoucher(user, group);

        //when
        Optional<WishList> result = wishListRepository.findByUserIdAndVoucherId(user.getId(),
                voucher.getId());

        //then
        assertThat(result).isNotPresent();
    }

    private User createNewUser() {
        User user = User.builder().build();
        return userRepository.save(user);
    }

    private Group createNewGroup(User user) {
        Group group = Group.createNewGroup(user, "TestCode", "테스트 그룹");
        return groupRepository.save(group);
    }

    private Voucher createVoucher(User user, Group group) {
        Voucher voucher = Voucher.createNewVoucher(user, group, "쿠폰", "imagekey",
                LocalDate.of(2020, 1, 1));
        return voucherRepository.save(voucher);
    }

    private WishList createWishList(User user, Voucher voucher, Group group) {
        return wishListRepository.save(
                WishList.builder().isActive(true).group(group).voucher(voucher).user(user).build());
    }

    private WishList createWishListWithNotActive(User user, Voucher voucher, Group group) {
        return wishListRepository.save(
                WishList.builder().isActive(false).voucher(voucher).group(group).user(user).build());
    }

}
