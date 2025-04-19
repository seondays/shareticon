package seondays.shareticon.voucher;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.exception.ExpiredVoucherException;
import seondays.shareticon.exception.GroupNotFoundException;
import seondays.shareticon.exception.InvalidAccessVoucherException;
import seondays.shareticon.exception.InvalidVoucherDeleteException;
import seondays.shareticon.exception.IllegalVoucherImageException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.exception.VoucherNotFoundException;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.voucher.dto.CreateVoucherRequest;
import seondays.shareticon.voucher.dto.VouchersResponse;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final ImageService imageService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final VoucherRepository voucherRepository;
    private final UserGroupRepository userGroupRepository;

    /**
     * 새로운 쿠폰을 등록합니다
     *
     * @param request
     * @param image
     */
    @Transactional
    public VouchersResponse register(CreateVoucherRequest request, Long userId, MultipartFile image) {
        Long groupId = request.groupId();

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        validateImageFile(image);
        validateUserInGroup(userId, groupId);

        Voucher voucher = createVoucherWithImage(user, group, image);

        return VouchersResponse.of(voucher);
    }

    /**
     * 이미지를 포함하는 쿠폰 객체를 생성합니다.
     *
     * @param user
     * @param group
     * @param image
     * @return
     */
    private Voucher createVoucherWithImage(User user, Group group, MultipartFile image) {
        Voucher voucher = Voucher.createAvailableStatus(user, group);
        voucherRepository.save(voucher);

        String imageUrl = imageService.uploadImage(image);
        voucher.saveImage(imageUrl);

        return voucherRepository.save(voucher);
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
        User voucherUser = voucher.getUser();

        validateVoucherOwner(userId, voucherUser);
        validateUserAndVoucherInGroup(userId, groupId, voucher);

        voucherRepository.delete(voucher);
    }

    /**
     * 특정 그룹 내에 등록된 조회가능한 상태의 쿠폰을 전체 조회합니다.
     *
     * @param userId
     * @param groupId
     * @return
     */
    public Slice<VouchersResponse> getAllVoucher(Long userId, Long groupId, Long cursorId, int size) {
        if (!userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new InvalidAccessVoucherException();
        }

        Pageable pageable = PageRequest.of(0, size);
        Slice<Voucher> vouchers = voucherRepository.findAllPageWithCursorByDesc(groupId,
                VoucherStatus.forDisplayVoucherStatus(), cursorId, pageable);

        return vouchers.map(VouchersResponse::of);
    }

    /**
     * 등록된 쿠폰의 상태를 변경 처리합니다.
     * 사용가능 쿠폰인 경우 사용완료로, 사용완료 쿠폰인 경우 사용가능으로 변경됩니다.
     * 만료 쿠폰에 변경을 시도하는 경우에는 예외가 발생합니다.
     *
     * @param userId
     * @param groupId
     * @param voucherId
     */
    @Transactional
    public void changeVoucherStatus(Long userId, Long groupId, Long voucherId) {
        if (!userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new InvalidAccessVoucherException();
        }

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(VoucherNotFoundException::new);
        VoucherStatus nowStatus = voucher.getStatus();

        if (nowStatus.equals(VoucherStatus.EXPIRED)) {
            throw new ExpiredVoucherException();
        }

        if (nowStatus.equals(VoucherStatus.AVAILABLE)) {
            voucher.changeStatus(VoucherStatus.USED);
        } else if (nowStatus.equals(VoucherStatus.USED)){
            voucher.changeStatus(VoucherStatus.AVAILABLE);
        }
    }

    /**
     * 사용자와 쿠폰이 동일한 그룹 내에 속해있는지 검증합니다.
     *
     * @param userId
     * @param groupId
     * @param voucher
     * @return
     */
    private void validateUserAndVoucherInGroup(Long userId, Long groupId, Voucher voucher) {
        if (!userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new InvalidVoucherDeleteException();
        }
        if (!voucher.getGroup().getId().equals(groupId)) {
            throw new InvalidVoucherDeleteException();
        }
    }

    /**
     * 사용자가 그룹에 속해있는지 검증합니다.
     *
     * @param userId
     * @param groupId
     * @return
     */
    private void validateUserInGroup(Long userId, Long groupId) {
        if (!userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new InvalidAccessVoucherException();
        }
    }

    /**
     * 해당 유저가 쿠폰을 등록한 유저인지 검증합니다.
     *
     * @param userId
     * @param voucherUser
     */
    private void validateVoucherOwner(Long userId, User voucherUser) {
        if (!voucherUser.getId().equals(userId)) {
            throw new InvalidVoucherDeleteException();
        }
    }

    /**
     * 유효한 이미지 파일의 MIME 타입이 image인지 검증합니다.
     *
     * @param image
     */
    private void validateImageFile(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalVoucherImageException();
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image")) {
            throw new IllegalVoucherImageException();
        }
    }
}
