package seondays.shareticon.wishlist;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.exception.business.GroupNotFoundException;
import seondays.shareticon.exception.business.UserNotFoundException;
import seondays.shareticon.exception.business.VoucherNotFoundException;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.utils.SliceResponse;
import seondays.shareticon.utils.validator.ValidationFacade;
import seondays.shareticon.utils.validator.dto.VoucherAccessValidationRequest;
import seondays.shareticon.voucher.Voucher;
import seondays.shareticon.voucher.VoucherRepository;
import seondays.shareticon.wishlist.dto.WishListResponse;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;
    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final GroupRepository groupRepository;
    private final ImageService imageService;
    private final ValidationFacade validationFacade;

    public SliceResponse<WishListResponse> getAllWishList(Long userId, Long cursorId, int size) {
        validationFacade.validateGetWishList(userId);
        Pageable pageable = Pageable.ofSize(size);
        Slice<WishList> wishLists = wishListRepository.findAllByUserId(userId, cursorId, pageable);

        Slice<WishListResponse> responses = wishLists.map(wishList -> {
            Voucher voucher = wishList.getVoucher();
            String presignedImageUrl = imageService.getPresignedImageUrl(voucher.getImage(), 5L);
            return WishListResponse.of(wishList, presignedImageUrl);
        });

        return SliceResponse.from(responses);
    }


    @Transactional
    public void toggleWishList(Long userId, Long groupId, Long voucherId) {
        VoucherAccessValidationRequest validationRequest =
                VoucherAccessValidationRequest.of(userId, groupId, voucherId);
        validationFacade.validateWishList(validationRequest);

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Voucher voucher = voucherRepository.findById(voucherId).orElseThrow(VoucherNotFoundException::new);
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        Optional<WishList> existingWishList = wishListRepository.findByUserIdAndVoucherId(userId,
                voucherId);

        WishList wishList = WishList.toggleActive(user, voucher, group, existingWishList);
        wishListRepository.save(wishList);
    }

}
