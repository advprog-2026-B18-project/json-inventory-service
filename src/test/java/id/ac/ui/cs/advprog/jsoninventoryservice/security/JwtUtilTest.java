package id.ac.ui.cs.advprog.jsoninventoryservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String rawSecret = "ini-isi-ngasal-aja-yang-penting-panjang-minimal-32-karakter-ya";
    private final String secret = Base64.getEncoder().encodeToString(rawSecret.getBytes());

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", secret);
    }

    @Test
    void testValidateToken_Success() {
        String token = Jwts.builder()
                .setSubject("user-123")
                .signWith(getSigningKey())
                .compact();

        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_ExpiredOrInvalid() {
        assertFalse(jwtUtil.validateToken("token.palsu.banget.kocak"));
    }

    @Test
    void testGetAccountIdFromToken_UsingSubject() {
        String expectedId = "user-uuid-123";
        String token = Jwts.builder()
                .setSubject(expectedId)
                .signWith(getSigningKey())
                .compact();

        assertEquals(expectedId, jwtUtil.getAccountIdFromToken(token));
    }

    @Test
    void testGetAccountIdFromToken_UsingUserIdClaim() {
        String expectedId = "user-uuid-456";
        String token = Jwts.builder()
                .claim("user_id", expectedId)
                .signWith(getSigningKey())
                .compact();

        assertEquals(expectedId, jwtUtil.getAccountIdFromToken(token));
    }

    @Test
    void testGetAccountIdFromToken_UsingIdClaim() {
        String expectedId = "user-uuid-789";
        String token = Jwts.builder()
                .claim("id", expectedId)
                .signWith(getSigningKey())
                .compact();

        assertEquals(expectedId, jwtUtil.getAccountIdFromToken(token));
    }
}