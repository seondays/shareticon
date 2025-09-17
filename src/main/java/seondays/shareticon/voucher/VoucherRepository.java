package seondays.shareticon.voucher;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long>, VoucherQueryDslRepository {

    Long countByUserIdAndIsDeletedFalse(Long userId);

    @Query("SELECT v FROM Voucher v WHERE v.id = :voucherId AND v.isDeleted = false")
    Optional<Voucher> findById(Long voucherId);
}
