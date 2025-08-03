package seondays.shareticon.wishlist;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.exception.VoucherNotFoundException;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.utils.SliceResponse;
import seondays.shareticon.utils.validator.ValidationFacade;
import seondays.shareticon.utils.validator.dto.VoucherAccessValidationRequest;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherRepository;
import seondays.shareticon.voucher.dto.VouchersResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final ImageService imageService;
    private final ValidationFacade validationFacade;

    public SliceResponse<VouchersResponse> getAllWishList(Long userId, Long cursorId, int size) {
        validationFacade.validateGetWishList(userId);
        Pageable pageable = Pageable.ofSize(size);
        Slice<WishList> wishLists = wishListRepository.findAllByUserId(userId, cursorId, pageable);

        Slice<VouchersResponse> responses = wishLists.map(wishList -> {
            Voucher voucher = wishList.getVoucher();
            String presignedImageUrl = imageService.getPresignedImageUrl(voucher.getImage(), 5L);
            return VouchersResponse.of(voucher, presignedImageUrl);
        });

        return SliceResponse.from(responses);
    }

}
