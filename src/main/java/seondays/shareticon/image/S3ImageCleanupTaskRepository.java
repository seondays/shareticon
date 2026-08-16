package seondays.shareticon.image;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface S3ImageCleanupTaskRepository extends JpaRepository<S3ImageCleanupTask, Long> {

    Optional<S3ImageCleanupTask> findByObjectKey(String objectKey);

    void deleteByObjectKey(String objectKey);

    List<S3ImageCleanupTask> findByCreatedDateTimeBeforeAndAttemptCountLessThan(
            LocalDateTime threshold, int maxAttempt, Pageable pageable);

}
