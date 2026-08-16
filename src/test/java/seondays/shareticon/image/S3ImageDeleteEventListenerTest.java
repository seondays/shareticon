package seondays.shareticon.image;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import seondays.shareticon.exception.business.ImageDeleteException;

@ExtendWith(MockitoExtension.class)
class S3ImageDeleteEventListenerTest {

    @Mock
    private ImageService imageService;

    @Mock
    private S3ImageCleanupService cleanupService;

    @InjectMocks
    private S3ImageDeleteEventListener listener;

    @Test
    @DisplayName("S3 삭제 성공 시 정리 기록을 제거한다")
    void successRemovesCleanupTask() {
        //when
        listener.onImageDelete(new S3ImageDeleteEvent("voucher/key"));

        //then
        verify(imageService).deleteImage("voucher/key");
        verify(cleanupService).complete("voucher/key");
    }

    @Test
    @DisplayName("S3 삭제 실패 시 정리 기록을 유지하고 예외를 전파하지 않는다")
    void failureKeepsCleanupTask() {
        //given
        doThrow(new ImageDeleteException("voucher/key"))
                .when(imageService).deleteImage("voucher/key");

        //when
        listener.onImageDelete(new S3ImageDeleteEvent("voucher/key"));

        //then
        verify(cleanupService, never()).complete(anyString());
    }
}
