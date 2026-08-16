package seondays.shareticon.voucher;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.group.Group;
import seondays.shareticon.image.S3ImageCleanupTaskRepository;
import seondays.shareticon.logging.VoucherEvent;
import seondays.shareticon.user.User;
import seondays.shareticon.voucher.dto.CreateVoucherRequest;

@Component
@RequiredArgsConstructor
public class VoucherFactory {

    private final VoucherRepository voucherRepository;
    private final S3ImageCleanupTaskRepository cleanupTaskRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 쿠폰 저장을 완료하는 트랜잭션 경계. 쿠폰을 저장하고, 등록 전 미리 남겨둔 정리 기록(보험)을 같은 트랜잭션에서 제거한 뒤 REGISTER 이벤트를
     * 발행한다. 저장이 실패하면 정리 기록 제거도 함께 롤백되어 업로드된 이미지는 정리 대상으로 남는다.
     */
    @Transactional
    public Voucher createVoucherWithImage(User user, Group group, CreateVoucherRequest request, String imageKey) {
        Voucher newVoucher = Voucher.createNewVoucher(user, group, request.voucherName(), imageKey,
                request.expiration());
        Voucher saved = voucherRepository.save(newVoucher);

        cleanupTaskRepository.deleteByObjectKey(imageKey);
        eventPublisher.publishEvent(VoucherEvent.toRegister(saved.getId(), group.getId()));

        return saved;
    }
}
