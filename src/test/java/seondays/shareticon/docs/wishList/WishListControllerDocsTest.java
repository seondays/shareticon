package seondays.shareticon.docs.wishList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessRequest;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import seondays.shareticon.docs.RestDocsSupport;
import seondays.shareticon.group.Group;
import seondays.shareticon.user.User;
import seondays.shareticon.utils.SliceResponse;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherStatus;
import seondays.shareticon.wishlist.WishList;
import seondays.shareticon.wishlist.WishListController;
import seondays.shareticon.wishlist.WishListService;
import seondays.shareticon.wishlist.dto.WishListResponse;

public class WishListControllerDocsTest extends RestDocsSupport {

    private final WishListService wishListService = mock(WishListService.class);

    @Override
    protected Object initController() {
        return new WishListController(wishListService);
    }

    @Test
    @DisplayName("전체 찜 내역을 조회한다")
    void getAllWishList() throws Exception {
        //given
        User user = createNewUser();
        Group group = createNewGroup(user);
        Voucher voucher = createNewVoucher(user, group);
        WishList wishList = createWishList(user, voucher, group);
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
                                .with(addBearerToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.content").exists())
                .andExpect(jsonPath("$.content.length()").value(mockSlice.getContent().size()))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.size").value(pageSize))
                .andDo(document("wishList-get",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("cursorId").description("페이지네이션 기반 조회를 위한 커서 ID")
                                        .optional(),
                                parameterWithName("pageSize").description("페이지 사이즈 값 (기본값 5)")
                                        .optional()
                        ),
                        responseFields(
                                fieldWithPath("content").type(JsonFieldType.ARRAY)
                                        .description("조회된 찜 목록에 속하는 쿠폰 객체 배열"),
                                fieldWithPath("content[].voucherId").type(JsonFieldType.NUMBER)
                                        .description("유저가 찜한 쿠폰 ID"),
                                fieldWithPath("content[].groupId").type(JsonFieldType.NUMBER)
                                                .description("유저가 찜한 쿠폰이 속한 그룹 ID"),
                                fieldWithPath("content[].presignedImage").type(JsonFieldType.STRING)
                                        .description("유저가 찜한 쿠폰 이미지 URL"),
                                fieldWithPath("content[].voucherName").type(JsonFieldType.STRING)
                                        .description("유저가 찜한 쿠폰 이름"),
                                fieldWithPath("content[].registeredUserId").type(
                                        JsonFieldType.NUMBER).description("쿠폰을 등록한 유저 ID"),
                                fieldWithPath("content[].expiration").type(JsonFieldType.STRING)
                                        .description("유저가 찜한 쿠폰의 만료일"),
                                fieldWithPath("content[].status").type(JsonFieldType.STRING)
                                        .description("유저가 찜한 쿠폰의 상태"),

                                fieldWithPath("hasNext").type(JsonFieldType.BOOLEAN)
                                        .description("다음 페이지가 존재하는지 여부"),
                                fieldWithPath("size").type(JsonFieldType.NUMBER)
                                        .description("요청한 페이지의 사이즈")
                        )
                ));

    }

    @Test
    @DisplayName("찜 내역의 상태를 변경한다")
    void toggleWishList() throws Exception {
        //given
        Long voucherId = 1L;
        Long groupId = 1L;

        //when //then
        mockMvc.perform(
                        MockMvcRequestBuilders.patch("/api/wishList/group/{groupId}/voucher/{voucherId}",
                                        groupId, voucherId)
                                .with(oauth2Login().oauth2User(mockUser))
                                .with(addBearerToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andDo(document("wishList-toggle",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("groupId").description("찜 대상이 되는 쿠폰이 속한 그룹 ID"),
                                parameterWithName("voucherId").description("찜 대상이 되는 쿠폰 ID"))
                ));

        verify(wishListService, times(1))
                .toggleWishList(eq(mockUser.getId()), eq(groupId), eq(voucherId));
    }


    private User createNewUser() {
        return User.builder().id(1L).build();
    }

    private Group createNewGroup(User user) {
        return Group.builder().id(1L).leaderUser(user).inviteCode("TestCode").title("테스트 그룹").build();
    }

    private Voucher createNewVoucher(User user, Group group) {
        return Voucher.builder().id(1L).user(user).group(group).name("쿠폰").status(
                        VoucherStatus.AVAILABLE).image("imagekey")
                .expiration(LocalDate.of(2020, 1, 1)).build();
    }

    private WishList createWishList(User user, Voucher voucher, Group group) {
        return WishList.builder().isActive(true).voucher(voucher).user(user).group(group).build();
    }

}
