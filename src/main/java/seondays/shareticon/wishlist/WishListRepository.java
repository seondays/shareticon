package seondays.shareticon.wishlist;

import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WishListRepository extends JpaRepository<WishList, Long> {

    @Query("""
            SELECT w FROM WishList w
            JOIN FETCH w.voucher
            WHERE w.user.id = :userId
            AND w.isActive = true
            AND (:cursorId is null or w.id < :cursorId)
            ORDER BY w.id DESC
            """)
    Slice<WishList> findAllByUserId(Long userId, Long cursorId, Pageable pageable);


    @Query("""
            SELECT w FROM WishList w
            WHERE w.user.id = :userId
            AND w.voucher.id = :voucherId
            """)
    Optional<WishList> findByUserIdAndVoucherId(Long userId, Long voucherId);
}