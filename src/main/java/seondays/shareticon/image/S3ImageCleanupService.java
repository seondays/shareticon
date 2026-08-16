package seondays.shareticon.image;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class S3ImageCleanupService {

    private final S3ImageCleanupTaskRepository cleanupTaskRepository;

    /**
     * 정리 대상 objectKey를 기록한다. 등록 흐름에서 호출되면 자체 트랜잭션으로 즉시 커밋되고, 삭제 흐름에서 호출되면 호출자의 트랜잭션에 합류해
     * 쿠폰 삭제 상태와 원자적으로 커밋된다.
     */
    @Transactional
    public void reserve(String objectKey) {
        cleanupTaskRepository.save(S3ImageCleanupTask.create(objectKey));
    }

    /**
     * objectKey의 정리 기록을 제거한다. S3 이미지 삭제가 성공했을 때 호출한다.
     */
    @Transactional
    public void complete(String objectKey) {
        cleanupTaskRepository.deleteByObjectKey(objectKey);
    }
}
