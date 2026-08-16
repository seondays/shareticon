package seondays.shareticon.image;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ImageDeleteEventListener {

    private final ImageService imageService;
    private final S3ImageCleanupService cleanupService;

    /**
     * 쿠폰 삭제 트랜잭션이 커밋된 뒤에만 S3 이미지를 삭제한다. 커밋 전 삭제를 막아, 삭제가 롤백되었는데 이미지만 사라지는 상황을 방지한다. 삭제에 성공하면
     * 정리 기록을 제거하고, 실패하면 기록을 남겨 배치가 재처리한다.
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onImageDelete(S3ImageDeleteEvent event) {
        String objectKey = event.objectKey();
        try {
            imageService.deleteImage(objectKey);
            cleanupService.complete(objectKey);
        } catch (Exception e) {
            log.warn("S3 이미지 즉시 삭제에 실패했습니다. 배치에서 재처리합니다. objectKey={}", objectKey, e);
        }
    }
}
