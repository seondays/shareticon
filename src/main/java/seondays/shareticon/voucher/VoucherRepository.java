package seondays.shareticon.voucher;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    List<Voucher> findAllByGroupIdAndStatusIn(Long groupId, List<VoucherStatus> voucherStatuses);
}
