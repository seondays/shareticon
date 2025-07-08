package seondays.shareticon.voucher;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import seondays.shareticon.group.Group;
import seondays.shareticon.user.User;
import seondays.shareticon.voucher.dto.CreateVoucherRequest;

@Component
@RequiredArgsConstructor
public class VoucherFactory {

    private final VoucherRepository voucherRepository;

    @Transactional
    public Voucher createVoucherWithImage(User user, Group group, CreateVoucherRequest request, String imageKey) {
        Voucher newVoucher = Voucher.createNewVoucher(user, group, request.voucherName(), imageKey,
                request.expiration());
        return voucherRepository.save(newVoucher);
    }
}
