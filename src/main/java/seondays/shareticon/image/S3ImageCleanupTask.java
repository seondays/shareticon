package seondays.shareticon.image;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import seondays.shareticon.utils.BaseEntity;

@Entity
@Table(name = "s3_image_cleanup_task",
        uniqueConstraints = @UniqueConstraint(name = "uk_s3_image_cleanup_task_object_key", columnNames = "object_key"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class S3ImageCleanupTask extends BaseEntity {

    static final int MAX_ERROR_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "last_error", length = MAX_ERROR_LENGTH)
    private String lastError;

    private S3ImageCleanupTask(String objectKey) {
        validateObjectKey(objectKey);
        this.objectKey = objectKey;
        this.attemptCount = 0;
    }

    private void validateObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            throw new IllegalArgumentException("정리 대상 objectKey는 비어 있을 수 없습니다");
        }
    }

    public static S3ImageCleanupTask create(String objectKey) {
        return new S3ImageCleanupTask(objectKey);
    }

    public void recordFailure(String errorMessage) {
        attemptCount++;
        lastError = truncate(errorMessage);
    }

    private String truncate(String errorMessage) {
        if (errorMessage == null || errorMessage.length() <= MAX_ERROR_LENGTH) {
            return errorMessage;
        }
        return errorMessage.substring(0, MAX_ERROR_LENGTH);
    }
}
