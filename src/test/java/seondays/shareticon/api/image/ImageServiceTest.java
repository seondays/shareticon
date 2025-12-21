package seondays.shareticon.api.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;
import seondays.shareticon.api.config.IntegrationTestSupport;
import seondays.shareticon.exception.business.ImageUploadException;
import seondays.shareticon.image.ImageService;
import seondays.shareticon.image.VoucherImage;
import seondays.shareticon.voucher.Voucher;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

public class ImageServiceTest extends IntegrationTestSupport {

    @MockitoBean
    private S3Client s3Client;

    @MockitoBean
    private S3Presigner s3Presigner;

    @Autowired
    private ImageService imageService;

    @Test
    @DisplayName("이미지를 정상 업로드하면 정상 key가 반환된다")
    void uploadImage() {
        //given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        VoucherImage voucherImage = VoucherImage.of(imageFile);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(mock(PutObjectResponse.class));

        //when
        String result = imageService.uploadImageWithRetry(voucherImage);

        //then
        assertThat(result).startsWith("voucher");

    }

    @Test
    @DisplayName("SdkClientException이 발생하는 실패는 최대 3회 재시도한다")
    void uploadImageWithSdkClientException() {
        //given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        VoucherImage voucherImage = VoucherImage.of(imageFile);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.class);

        //when
        assertThatThrownBy(() -> imageService.uploadImageWithRetry(voucherImage))
                .isInstanceOf(ImageUploadException.class);

        //then
        verify(s3Client, times(3))
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

    }

    @Test
    @DisplayName("인증 실패나 권한 없음 등, S3Exception이 발생하는 실패는 재시도하지 않고 저장이 최종 실패한다")
    void uploadImageWithS3Exception() {
        //given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        VoucherImage voucherImage = VoucherImage.of(imageFile);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.class);

        //when
        assertThatThrownBy(() -> imageService.uploadImageWithRetry(voucherImage))
                .isInstanceOf(ImageUploadException.class);

        //then
        verify(s3Client, times(1))
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

    }

    @Test
    @DisplayName("IOException이 발생하는 실패는 재시도하지 않고 바로 저장이 최종 실패한다")
    void uploadImageWithIOExcepiton() throws IOException {
        //given
        MultipartFile imageFile = mock(MultipartFile.class);
        when(imageFile.getContentType()).thenReturn("image");
        when(imageFile.getOriginalFilename()).thenReturn("image.png");
        when(imageFile.getInputStream()).thenThrow(IOException.class);

        VoucherImage voucherImage = VoucherImage.of(imageFile);

        //when
        assertThatThrownBy(() -> imageService.uploadImageWithRetry(voucherImage))
                .isInstanceOf(ImageUploadException.class);

        //then
        verify(s3Client, never())
                .putObject(any(PutObjectRequest.class), any(RequestBody.class));

    }

    @Test
    @DisplayName("이미지 업로드 실패 시, 3회 재시도 중 1회만 성공하면 최종 성공한다")
    void uploadImageSucceedWithRetry() {
        //given
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test".getBytes()
        );

        VoucherImage voucherImage = VoucherImage.of(imageFile);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.class).thenReturn(mock(PutObjectResponse.class));

        //when
        String result = imageService.uploadImageWithRetry(voucherImage);

        //then
        assertThat(result).startsWith("voucher");

    }

    @Test
    @DisplayName("저장 key를 가지고 presigned URL을 생성한다")
    void makePresignedImageUrl() throws MalformedURLException {
        //given
        String expectedUrl = "http://www.image.com";
        PresignedGetObjectRequest mockRequest = mock(PresignedGetObjectRequest.class);

        when(mockRequest.url()).thenReturn(new URL(expectedUrl));

        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(
                mockRequest);

        String objectKey = "key";
        Long expiration = 1L;

        //when
        String result = imageService.getPresignedImageUrl(objectKey, expiration);

        //then
        assertThat(result).isEqualTo(expectedUrl);
        verify(s3Presigner).presignGetObject(any(GetObjectPresignRequest.class));

    }

    @Test
    @DisplayName("이미지를 정상 삭제한다")
    void deleteImageSuccess() {
        //given
        String imageKey = "imageKey";
        Voucher voucher = Voucher.builder().image(imageKey).build();

        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenReturn(mock(
                DeleteObjectResponse.class));

        //when
        imageService.deleteImage(voucher);

        //then
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));

    }

    @Test
    @DisplayName("이미지 삭제 시, SdkClientException이 발생하는 실패는 최대 3회 재시도한다")
    void deleteImageWithSdkClientException() {
        //given
        String imageKey = "imageKey";
        Voucher voucher = Voucher.builder().image(imageKey).build();

        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(SdkClientException.class);

        //when
        imageService.deleteImage(voucher);

        //then
        verify(s3Client, times(3)).deleteObject(any(DeleteObjectRequest.class));

    }

    @Test
    @DisplayName("이미지 삭제 시, S3Exception이 발생하는 실패는 재시도하지 않고 실패한다")
    void deleteImageWithS3Exception() {
        //given
        String imageKey = "imageKey";
        Voucher voucher = Voucher.builder().image(imageKey).build();

        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(S3Exception.class);

        //when
        imageService.deleteImage(voucher);

        //then
        verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));

    }

    @Test
    @DisplayName("이미지 삭제 실패 시, 3회 재시도 중 1회만 성공하면 최종 성공한다")
    void deleteImageSucceedWithRetry() {
        //given
        String imageKey = "imageKey";
        Voucher voucher = Voucher.builder().image(imageKey).build();

        when(s3Client.deleteObject(any(DeleteObjectRequest.class))).thenThrow(SdkClientException.class)
                .thenReturn(any(DeleteObjectResponse.class));

        //when
        imageService.deleteImage(voucher);

        //then
        verify(s3Client, times(2)).deleteObject(any(DeleteObjectRequest.class));

    }

}
