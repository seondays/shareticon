package seondays.shareticon.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class S3ImageCleanupTaskTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("objectKey가 null이거나 비어 있으면 생성에 실패한다")
    void createRejectsBlankObjectKey(String objectKey) {
        //when //then
        assertThatThrownBy(() -> S3ImageCleanupTask.create(objectKey))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("create는 attemptCount 0, lastError null 상태의 정리 작업을 생성한다")
    void createInitializesTask() {
        //when
        S3ImageCleanupTask task = S3ImageCleanupTask.create("voucher/key");

        //then
        assertThat(task.getObjectKey()).isEqualTo("voucher/key");
        assertThat(task.getAttemptCount()).isZero();
        assertThat(task.getLastError()).isNull();
    }

    @Test
    @DisplayName("recordFailure는 시도 횟수를 증가시키고 마지막 에러를 기록한다")
    void recordFailureIncrementsAttemptAndStoresError() {
        //given
        S3ImageCleanupTask task = S3ImageCleanupTask.create("voucher/key");

        //when
        task.recordFailure("timeout");

        //then
        assertThat(task.getAttemptCount()).isEqualTo(1);
        assertThat(task.getLastError()).isEqualTo("timeout");
    }

    @Test
    @DisplayName("recordFailure를 여러 번 호출하면 시도 횟수가 누적된다")
    void recordFailureAccumulatesAttemptCount() {
        //given
        S3ImageCleanupTask task = S3ImageCleanupTask.create("voucher/key");

        //when
        task.recordFailure("first");
        task.recordFailure("second");

        //then
        assertThat(task.getAttemptCount()).isEqualTo(2);
        assertThat(task.getLastError()).isEqualTo("second");
    }

    @Test
    @DisplayName("에러 메시지가 컬럼 길이를 초과하면 잘라서 저장한다")
    void recordFailureTruncatesLongError() {
        //given
        S3ImageCleanupTask task = S3ImageCleanupTask.create("voucher/key");
        String longMessage = "x".repeat(S3ImageCleanupTask.MAX_ERROR_LENGTH + 100);

        //when
        task.recordFailure(longMessage);

        //then
        assertThat(task.getLastError()).hasSize(S3ImageCleanupTask.MAX_ERROR_LENGTH);
    }
}
