package seondays.shareticon.api.image;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import seondays.shareticon.api.config.RepositoryTestSupport;
import seondays.shareticon.image.S3ImageCleanupTask;
import seondays.shareticon.image.S3ImageCleanupTaskRepository;

class S3ImageCleanupTaskRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private S3ImageCleanupTaskRepository cleanupTaskRepository;

    @Test
    @DisplayName("objectKey로 정리 작업을 조회한다")
    void findByObjectKey() {
        //given
        cleanupTaskRepository.save(S3ImageCleanupTask.create("voucher/key"));

        //when
        var found = cleanupTaskRepository.findByObjectKey("voucher/key");

        //then
        assertThat(found).isPresent();
        assertThat(found.get().getObjectKey()).isEqualTo("voucher/key");
    }

    @Test
    @DisplayName("objectKey로 정리 작업을 삭제한다")
    void deleteByObjectKey() {
        //given
        cleanupTaskRepository.save(S3ImageCleanupTask.create("voucher/key"));

        //when
        cleanupTaskRepository.deleteByObjectKey("voucher/key");

        //then
        assertThat(cleanupTaskRepository.findByObjectKey("voucher/key")).isEmpty();
    }
}
