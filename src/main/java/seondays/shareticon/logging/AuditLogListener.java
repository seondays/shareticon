package seondays.shareticon.logging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVoucher(VoucherEvent event) {
        log.info("[AUDIT] --- domain=Voucher action={} voucherId={} groupId={}",
                event.action(), event.voucherId(), event.groupId());
    }

}
