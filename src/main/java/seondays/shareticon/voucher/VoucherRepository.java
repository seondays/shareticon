package seondays.shareticon.voucher;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    @Query("""
            select v from Voucher v
            where v.group.id= :groupId
            and v.status in :voucherStatuses
            and (:cursorId is null or v.id < :cursorId)
            and v.isDeleted = false
            order by v.id DESC
            """)
    Slice<Voucher> findAllPageWithCursorByDesc(@Param("groupId") Long groupId,
            @Param("voucherStatuses") List<VoucherStatus> voucherStatuses,
            @Param("cursorId") Long cursorId, Pageable pageable);

    Long countByUserIdAndIsDeletedFalse(Long userId);

    @Query("SELECT v FROM Voucher v WHERE v.id = :voucherId AND v.isDeleted = false")
    Optional<Voucher> findById(Long voucherId);
}
