package seondays.shareticon.voucher;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.exception.business.GroupNotFoundException;
import seondays.shareticon.exception.business.InvalidAccessException;
import seondays.shareticon.exception.business.UserNotFoundException;
import seondays.shareticon.exception.business.VoucherNotFoundException;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.image.VoucherImage;
import seondays.shareticon.logging.VoucherEvent;
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
    private final ApplicationEventPublisher eventPublisher;

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
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        VoucherCreationValidationRequest validationRequest = VoucherCreationValidationRequest.of(
                userId, groupId, request.expiration());
        validationFacade.validateVoucherCreation(validationRequest);

        VoucherImage voucherImage = VoucherImage.of(image);
        String imageKey = imageService.uploadImageWithRetry(voucherImage);

        Voucher voucher = voucherFactory.createVoucherWithImage(user, group, request, imageKey);

        String preSignedUrl = imageService.getPresignedImageUrl(voucher.getImage(), 5L);

        eventPublisher.publishEvent(VoucherEvent.toRegister(voucher.getId(), groupId));
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
                .orElseThrow(() -> new VoucherNotFoundException(voucherId));

        VoucherDeletionValidationRequest validationRequest =
                VoucherDeletionValidationRequest.of(userId, groupId, voucher);
        validationFacade.validateVoucherDeletion(validationRequest);

        voucher.delete();

        eventPublisher.publishEvent(VoucherEvent.toDelete(voucher.getId(), groupId));
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
                .orElseThrow(() -> new InvalidAccessException(groupId));

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
     * 쿠폰을 사용 완료 상태로 변경합니다. 만료된 쿠폰에 호출되면 예외가 발생합니다.
     *
     * @param userId
     * @param groupId
     * @param voucherId
     */
    @Transactional
    public void markAsUsed(Long userId, Long groupId, Long voucherId) {
        VoucherAccessValidationRequest validationRequest =
                VoucherAccessValidationRequest.of(userId, groupId, voucherId);
        validationFacade.validateAccessVoucher(validationRequest);

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new VoucherNotFoundException(voucherId));

        if (voucher.markAsUsed()) {
            eventPublisher.publishEvent(VoucherEvent.toChangeStatus(voucher, groupId));
        }
    }

    /**
     * 쿠폰을 사용 가능 상태로 변경합니다. 만료된 쿠폰에 호출되면 예외가 발생합니다.
     *
     * @param userId
     * @param groupId
     * @param voucherId
     */
    @Transactional
    public void markAsAvailable(Long userId, Long groupId, Long voucherId) {
        VoucherAccessValidationRequest validationRequest =
                VoucherAccessValidationRequest.of(userId, groupId, voucherId);
        validationFacade.validateAccessVoucher(validationRequest);

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new VoucherNotFoundException(voucherId));

        if (voucher.markAsAvailable()) {
            eventPublisher.publishEvent(VoucherEvent.toChangeStatus(voucher, groupId));
        }
    }

}
