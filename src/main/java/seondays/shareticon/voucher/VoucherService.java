package seondays.shareticon.voucher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.exception.GroupNotFoundException;
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
