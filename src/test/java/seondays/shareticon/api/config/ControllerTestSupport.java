package seondays.shareticon.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import seondays.shareticon.group.GroupController;
import seondays.shareticon.group.GroupService;
import seondays.shareticon.login.CustomOAuth2User;
import seondays.shareticon.login.token.TokenController;
import seondays.shareticon.login.token.TokenService;
import seondays.shareticon.user.UserController;
import seondays.shareticon.user.UserService;
import seondays.shareticon.voucher.VoucherController;
import seondays.shareticon.voucher.VoucherService;
import seondays.shareticon.wishlist.WishListController;
import seondays.shareticon.wishlist.WishListService;

@WebMvcTest(controllers = {
        VoucherController.class,
        GroupController.class,
        TokenController.class,
        UserController.class,
        WishListController.class})
@Import({TestSecurityConfig.class, TestValidatorConfig.class})
public abstract class ControllerTestSupport {

    @MockitoBean
    protected VoucherService voucherService;

    @MockitoBean
    protected GroupService groupService;

    @MockitoBean
    protected TokenService tokenService;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected WishListService wishListService;

    @Autowired
    protected MockMvc mockMvc;

    protected CustomOAuth2User mockUser;
}
