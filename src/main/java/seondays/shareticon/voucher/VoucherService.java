package seondays.shareticon.voucher;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.exception.GroupNotFoundException;
import seondays.shareticon.exception.NoVoucherImageException;
import seondays.shareticon.exception.UserNotFoundException;
import seondays.shareticon.group.Group;
import seondays.shareticon.group.GroupRepository;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.user.User;
import seondays.shareticon.user.UserRepository;

@Service
public class VoucherService {

    private final ImageService imageService;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final VoucherRepository voucherRepository;

    public VoucherService(ImageService imageService, UserRepository userRepository,
            GroupRepository groupRepository, VoucherRepository voucherRepository) {
        this.imageService = imageService;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.voucherRepository = voucherRepository;
    }

    /**
     * 새로운 쿠폰을 등록합니다
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

}
