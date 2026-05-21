package id.ac.ui.cs.advprog.jsoninventoryservice.config;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.services.s3.S3Client;

class S3ConfigTest {

    private S3Config s3Config;

    @BeforeEach
    void setUp() {
        s3Config = new S3Config();
        ReflectionTestUtils.setField(s3Config, "accessKey", "dummyAccessKey");
        ReflectionTestUtils.setField(s3Config, "secretKey", "dummySecretKey");
        ReflectionTestUtils.setField(s3Config, "region", "eu-west-1");
    }

    @ParameterizedTest
    @MethodSource("provideS3ConfigTestData")
    void testS3ClientConfigurations(String sessionToken, String endpointOverride) {
        ReflectionTestUtils.setField(s3Config, "sessionToken", sessionToken);
        ReflectionTestUtils.setField(s3Config, "endpointOverride", endpointOverride);

        S3Client client = s3Config.s3Client();
        assertNotNull(client);
    }

    private static Stream<Arguments> provideS3ConfigTestData() {
        return Stream.of(
            arguments("none", ""),
            arguments(null, null),
            arguments("   ", "   "),
            arguments("valid-long-session-token-12345", "http://localhost:4566")
        );
    }
}