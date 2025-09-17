package seondays.shareticon.voucher;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.exception.GroupNotFoundException;
import seondays.shareticon.exception.InvalidAccessException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.exception.VoucherNotFoundException;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.image.VoucherImage;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroup;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.utils.SliceResponse;
import seondays.shareticon.utils.validator.ValidationFacade;
import seondays.shareticon.voucher.dto.CreateVoucherRequest;
import seondays.shareticon.utils.validator.dto.VoucherCreationValidationRequest;
import seondays.shareticon.utils.validator.dto.VoucherDeletionValidationRequest;
import seondays.shareticon.voucher.dto.VoucherListResponse;
import seondays.shareticon.utils.validator.dto.VoucherAccessValidationRequest;
import seondays.shareticon.voucher.dto.VoucherWithWishListResponse;
import seondays.shareticon.voucher.dto.VouchersResponse;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class VoucherService {

    private final ImageService imageService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final VoucherRepository voucherRepository;
    private final UserGroupRepository userGroupRepository;
    private final ValidationFacade validationFacade;
    private final VoucherFactory voucherFactory;

    /**
     * 새로운 쿠폰을 등록합니다.
     *
     * @param request
     * @param userId
     * @param image
     * @return
     */
    @Transactional
    public VouchersResponse register(CreateVoucherRequest request, Long userId,
            MultipartFile image) {
        Long groupId = request.groupId();
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        VoucherCreationValidationRequest validationRequest = VoucherCreationValidationRequest.of(
                userId, groupId, request.expiration());
        validationFacade.validateVoucherCreation(validationRequest);

        VoucherImage voucherImage = VoucherImage.of(image);
        String imageKey = imageService.uploadImageWithRetry(voucherImage);

        Voucher voucher = voucherFactory.createVoucherWithImage(user, group, request, imageKey);

        String preSignedUrl = imageService.getPresignedImageUrl(voucher.getImage(), 5L);
        return VouchersResponse.withWishList(voucher, preSignedUrl, false);
    }

    /**
     * 등록된 쿠폰을 삭제합니다. 쿠폰을 등록한 사용자만 쿠폰의 삭제가 가능합니다.
     *
     * @param userId
     * @param groupId
     * @param voucherId
     */
    @Transactional
    public void delete(Long userId, Long groupId, Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(VoucherNotFoundException::new);

        VoucherDeletionValidationRequest validationRequest =
                VoucherDeletionValidationRequest.of(userId, groupId, voucher);
        validationFacade.validateVoucherDeletion(validationRequest);

        voucher.delete();

        imageService.deleteImageAsync(voucher);
    }

    /**
     * 특정 그룹 내에 등록된 조회가능한 상태의 쿠폰을 전체 조회합니다.
     *
     * @param userId
     * @param groupId
     * @param cursorId
     * @param size
     * @return
     */
    public SliceResponse<VoucherListResponse> getAllVoucher(Long userId, Long groupId, Long cursorId,
            int size, VoucherFilterCondition condition) {
        UserGroup userGroup = userGroupRepository.findByUserIdAndGroupId(userId, groupId)
                .orElseThrow(InvalidAccessException::new);

        Pageable pageable = PageRequest.ofSize(size);

        Slice<VoucherWithWishListResponse> vouchers =
                voucherRepository.searchVoucher(userId, groupId, condition, cursorId, pageable);

        List<VouchersResponse> vouchersResponseList = vouchers.stream()
                .map(voucher -> {
                    String preSignedUrl = imageService.getPresignedImageUrl(voucher.image(), 5L);
                    return VouchersResponse.of(voucher, preSignedUrl);
                }).toList();

        VoucherListResponse voucherListResponse = VoucherListResponse.of(vouchersResponseList,
                userGroup);

        return SliceResponse.of(voucherListResponse, vouchers.hasNext(), pageable.getPageSize());
    }

    /**
     * 등록된 쿠폰의 상태를 변경 처리합니다. 사용가능 쿠폰인 경우 사용완료로, 사용완료 쿠폰인 경우 사용가능으로 변경됩니다. 만료 쿠폰에 변경을 시도하는 경우에는 예외가
     * 발생합니다.
     *
     * @param userId
     * @param groupId
     * @param voucherId
     */
    @Transactional
    public void changeVoucherStatus(Long userId, Long groupId, Long voucherId) {

        VoucherAccessValidationRequest validationRequest =
                VoucherAccessValidationRequest.of(userId, groupId, voucherId);
        validationFacade.validateAccessVoucher(validationRequest);

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(VoucherNotFoundException::new);

        voucher.changeStatus();
    }

}
