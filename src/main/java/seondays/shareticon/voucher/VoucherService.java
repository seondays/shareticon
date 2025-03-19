package seondays.shareticon.voucher;

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
import seondays.shareticon.exception.NoVoucherImageException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.exception.VoucherNotFoundException;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;
import seondays.shareticon.userGroup.UserGroupRepository;
import seondays.shareticon.voucher.dto.VouchersResponse;

@Service
public class VoucherService {

    private final ImageService imageService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final VoucherRepository voucherRepository;
    private final UserGroupRepository userGroupRepository;

    public VoucherService(ImageService imageService, UserRepository userRepository,
            GroupRepository groupRepository, VoucherRepository voucherRepository,
            UserGroupRepository userGroupRepository) {
        this.imageService = imageService;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.voucherRepository = voucherRepository;
        this.userGroupRepository = userGroupRepository;
    }

    /**
     * 새로운 쿠폰을 등록합니다
     *
     * @param userId
     * @param groupId
     * @param image
     */
    @Transactional
    public void register(String userId, Long groupId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new NoVoucherImageException();
        }

        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        Voucher voucher = Voucher.builder()
                .group(group)
                .user(user)
                .status(VoucherStatus.AVAILABLE)
                .build();
        voucherRepository.save(voucher);

        String imageUrl = imageService.uploadImage(image);

        voucher.saveImage(imageUrl);
        voucherRepository.save(voucher);
    }

    /**
     * 등록된 쿠폰을 삭제합니다. 쿠폰을 등록한 사용자만 쿠폰의 삭제가 가능합니다.
     *
     * @param userId
     * @param groupId
     * @param voucherId
     */
    @Transactional
    public void delete(String userId, Long groupId, Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(VoucherNotFoundException::new);

        User voucherUser = voucher.getUser();
        if (!voucherUser.getId().equals(userId)) {
            throw new InvalidVoucherDeleteException();
        }
        if (!validateUserAndVoucherInGroup(userId, groupId, voucherId)) {
            throw new InvalidVoucherDeleteException();
        }

        voucherRepository.delete(voucher);
    }

    /**
     * 특정 그룹 내에 등록된 조회가능한 상태의 쿠폰을 전체 조회합니다.
     *
     * @param userId
     * @param groupId
     * @return
     */
    public Slice<VouchersResponse> getAllVoucher(String userId, Long groupId, Long cursorId, int size) {
        if (!userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new InvalidAccessVoucherException();
        }

        Pageable pageable = PageRequest.of(0, size);
        Slice<Voucher> vouchers = voucherRepository.findAllPageWithCursor(groupId,
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
    public void changeVoucherStatus(String userId, Long groupId, Long voucherId) {
        if (!userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            throw new InvalidAccessVoucherException();
        }

        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(VoucherNotFoundException::new);
        VoucherStatus nowStatus = voucher.getStatus();

        if (nowStatus.equals(VoucherStatus .EXPIRED)) {
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
     * @param voucherId
     * @return
     */
    private boolean validateUserAndVoucherInGroup(String userId, Long groupId, Long voucherId) {
        if (!userGroupRepository.existsByUserIdAndGroupId(userId, groupId)) {
            return false;
        }
        if (!voucherId.equals(groupId)) {
            return false;
        }
        return true;
    }
}
