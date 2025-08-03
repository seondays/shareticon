package seondays.shareticon.wishlist;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import seondays.shareticon.login.CustomOAuth2User;
import seondays.shareticon.utils.SliceResponse;
import seondays.shareticon.voucher.dto.VouchersResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishList")
public class WishListController {

    private final WishListService wishListService;

    @GetMapping
    public ResponseEntity<SliceResponse<VouchersResponse>> getAllWishList(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "5") int pageSize) {

        Long userId = userDetails.getId();
        SliceResponse<VouchersResponse> allWishList = wishListService.getAllWishList(userId, cursorId,
                pageSize);

        return ResponseEntity.ok(allWishList);
    }

    @PatchMapping("/group/{groupId}/voucher/{voucherId}")
    public ResponseEntity<Void> toggleWishListStatus(
            @AuthenticationPrincipal CustomOAuth2User userDetails,
            @PathVariable("groupId") Long groupId, @PathVariable("voucherId") Long voucherId) {

        Long userId = userDetails.getId();
        wishListService.toggleWishList(userId, groupId, voucherId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
