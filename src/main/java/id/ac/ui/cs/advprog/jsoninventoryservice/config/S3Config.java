package id.ac.ui.cs.advprog.jsoninventoryservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.s3.access-key:none}")
    private String accessKey;

    @Value("${aws.s3.secret-key:none}")
    private String secretKey;

    @Value("${aws.s3.session-token:none}")
    private String sessionToken;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.endpoint:}")
    private String endpointOverride;

    @Bean
    public S3Client s3Client() {
        AwsCredentialsProvider credentialsProvider;

        if (sessionToken == null || sessionToken.equals("none") || sessionToken.trim().isEmpty()) {
            credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            );
        } else {
            credentialsProvider = StaticCredentialsProvider.create(
                AwsSessionCredentials.create(accessKey, secretKey, sessionToken)
            );
        }

        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider);

        if (endpointOverride != null && !endpointOverride.trim().isEmpty()) {
            builder.endpointOverride(java.net.URI.create(endpointOverride));
            builder.serviceConfiguration(S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build());
        }

        return builder.build();
    }
}