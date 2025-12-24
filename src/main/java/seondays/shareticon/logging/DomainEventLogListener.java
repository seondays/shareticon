package seondays.shareticon.logging;

import static net.logstash.logback.argument.StructuredArguments.kv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventLogListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVoucher(VoucherEvent event) {
        log.info("[DOMAIN_EVENT] {} {} {} {}",
                kv("domain", "Voucher"),
                kv("action", event.action()),
                kv("voucherId", event.voucherId()),
                kv("groupId", event.groupId()));
    }

}
