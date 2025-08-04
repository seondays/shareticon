package seondays.shareticon.api.wishlist;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seondays.shareticon.api.config.ControllerTestSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.login.CustomOAuth2User;
import seondays.shareticon.user.User;
import seondays.shareticon.user.dto.UserOAuth2Dto;
import seondays.shareticon.utils.SliceResponse;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.wishlist.WishList;
import seondays.shareticon.wishlist.dto.WishListResponse;

public class WishListControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockUser = new CustomOAuth2User(new UserOAuth2Dto(1L, "user", "ROLE_USER"));
    }

    @Test
    @DisplayName("전체 찜 내역을 조회한다")
    void getAllWishList() throws Exception {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        Voucher voucher = createNewVoucher(user, group, 1L);
        WishList wishList = createWishList(user, group, voucher);
        String mockPresignedUrl = "mockPresignedUrl";
        Long cursorId = 1L;
        int pageSize = 3;

        WishListResponse mockResponse = WishListResponse.of(wishList, mockPresignedUrl);

        SliceResponse<WishListResponse> mockSlice = SliceResponse.of(
                mockResponse, false, pageSize);

        when(wishListService.getAllWishList(any(Long.class), any(Long.class), anyInt()))
                .thenReturn(mockSlice);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/wishList")
                                .param("cursorId", cursorId.toString())
                                .param("pageSize", String.valueOf(pageSize))
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(mockSlice.getContent().size()))
                .andExpect(jsonPath("$.size").value(pageSize));

    }

    @Test
    @DisplayName("찜 내역을 조회할 때 cursorId와 pageSize를 명시하지 않으면 기본 값이 적용된다")
    void getAllWishListWithDefault() throws Exception {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        Voucher voucher = createNewVoucher(user, group, 1L);
        WishList wishList = createWishList(user, group, voucher);
        String mockPresignedUrl = "mockPresignedUrl";

        int defaultPageSize = 5;

        WishListResponse mockResponse = WishListResponse.of(wishList, mockPresignedUrl);

        SliceResponse<WishListResponse> mockSlice = SliceResponse.of(
                mockResponse, false, defaultPageSize);

        when(wishListService.getAllWishList(any(Long.class), isNull(), eq(defaultPageSize)))
                .thenReturn(mockSlice);

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/wishList")
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.size").value(defaultPageSize));

        verify(wishListService)
                .getAllWishList(any(Long.class), isNull(), eq(defaultPageSize));

    }

    @Test
    @DisplayName("찜 내역의 상태를 변경한다")
    void toggleWishList() throws Exception {
        //given
        Long voucherId = 1L;
        Long groupId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/wishList/group/{groupId}/voucher/{voucherId}", groupId,
                                        voucherId)
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        verify(wishListService, times(1))
                .toggleWishList(eq(mockUser.getId()), eq(groupId), eq(voucherId));
    }

    @Test
    @DisplayName("찜 내역 상태 변경 시 그룹 아이디는 숫자 형식이어야 한다")
    void toggleWishListWithGroupIdLong() throws Exception {
        //given
        Long voucherId = 1L;
        String groupId = "abcd";

        //when
        mockMvc.perform(
                MockMvcRequestBuilders.patch("/api/wishList/group/{groupId}/voucher/{voucherId}", groupId,
                        voucherId)
                        .with(oauth2Login().oauth2User(mockUser))
        )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));
    }

    @Test
    @DisplayName("찜 내역 상태 변경 시 쿠폰 아이디는 숫자 형식이어야 한다")
    void toggleWishListWithVoucherIdLong() throws Exception {
        String voucherId = "voucherId";
        Long groupId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/wishList/group/{groupId}/voucher/{voucherId}", groupId,
                                        voucherId)
                                .with(oauth2Login().oauth2User(mockUser))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"));

    }

    private User createNewUser() {
        User user = User.builder().build();
        return user;
    }

    private Group createNewGroup(User user) {
        Group group = Group.createNewGroup(user, "TestCode", "테스트 그룹");
        return group;
    }

    private Voucher createNewVoucher(User user, Group group, Long id) {
        Voucher voucher = Voucher.builder().id(id).user(user).group(group).name("쿠폰")
                .image("imagekey").expiration(LocalDate.of(2020, 1, 1)).build();
        return voucher;
    }

    private WishList createWishList(User user, Group group, Voucher voucher) {
        WishList wishList = WishList.builder().isActive(true).voucher(voucher).user(user).group(group).build();
        return wishList;
    }

}
