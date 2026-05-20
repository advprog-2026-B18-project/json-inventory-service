package id.ac.ui.cs.advprog.jsoninventoryservice.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class S3ConfigTest {

    private S3Config s3Config;

    @BeforeEach
    void setUp() {
        s3Config = new S3Config();
        ReflectionTestUtils.setField(s3Config, "accessKey", "dummyAccessKey");
        ReflectionTestUtils.setField(s3Config, "secretKey", "dummySecretKey");
        ReflectionTestUtils.setField(s3Config, "region", "eu-west-1");
    }

    @Test
    void testS3ClientWithSessionTokenNoneAndNoEndpoint() {
        ReflectionTestUtils.setField(s3Config, "sessionToken", "none");
        ReflectionTestUtils.setField(s3Config, "endpointOverride", "");

        S3Client client = s3Config.s3Client();
        assertNotNull(client);
    }

    @Test
    void testS3ClientWithNullSessionTokenAndNullEndpoint() {
        ReflectionTestUtils.setField(s3Config, "sessionToken", null);
        ReflectionTestUtils.setField(s3Config, "endpointOverride", null);

        S3Client client = s3Config.s3Client();
        assertNotNull(client);
    }

    @Test
    void testS3ClientWithEmptySessionTokenAndEmptyEndpoint() {
        ReflectionTestUtils.setField(s3Config, "sessionToken", "   ");
        ReflectionTestUtils.setField(s3Config, "endpointOverride", "   ");

        S3Client client = s3Config.s3Client();
        assertNotNull(client);
    }

    @Test
    void testS3ClientWithValidSessionTokenAndValidEndpoint() {
        ReflectionTestUtils.setField(s3Config, "sessionToken", "valid-long-session-token-12345");
        ReflectionTestUtils.setField(s3Config, "endpointOverride", "http://localhost:4566");

        S3Client client = s3Config.s3Client();
        assertNotNull(client);
    }
}