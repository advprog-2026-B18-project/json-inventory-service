package id.ac.ui.cs.advprog.jsoninventoryservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "ini-isi-ngasal-aja-yang-penting-panjang";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", secret);
    }

    @Test
    void testValidateToken_Success() {
        String token = Jwts.builder()
                .setSubject("user-123")
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_ExpiredOrInvalid() {
        assertFalse(jwtUtil.validateToken("token.ngawur.banget"));
    }

    @Test
    void testGetAccountIdFromToken_UsingSubject() {
        String expectedId = "user-uuid-123";
        String token = Jwts.builder()
                .setSubject(expectedId)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();

        assertEquals(expectedId, jwtUtil.getAccountIdFromToken(token));
    }

    @Test
    void testGetAccountIdFromToken_UsingCustomClaim() {
        String expectedId = "user-uuid-456";
        String token = Jwts.builder()
                .claim("user_id", expectedId)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();

        assertEquals(expectedId, jwtUtil.getAccountIdFromToken(token));
    }

    @Test
    void testGetAccountIdFromToken_UsingIdClaim() {
        String expectedId = "user-uuid-789";
        String token = Jwts.builder()
                .claim("id", expectedId)
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();

        assertEquals(expectedId, jwtUtil.getAccountIdFromToken(token));
    }

    @Test
    void testValidateToken_NullOrEmptyToken() {
        assertFalse(jwtUtil.validateToken(null));
        assertFalse(jwtUtil.validateToken(""));
    }
}