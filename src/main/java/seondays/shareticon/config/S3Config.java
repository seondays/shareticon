package seondays.shareticon.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    // AWS 인증 정보를 가져오는 변수
    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    @Value("${aws.s3.region}")
    private String region;

    @Bean
    public S3Client amazonS3Client() {
        // AWS 자격 증명 객체 생성
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);

        // Amazon S3 클라이언트 빌더를 사용하여 클라이언트 구성
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

}
