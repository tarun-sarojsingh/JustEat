package com.justeat.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
                "test-secret-key-must-be-long-enough-for-hmac-sha-256-signing");
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", 3600000L);
    }

    // Test 12: Generate token and extract username -> username matches original input
    @Test
    void generateToken_thenExtractUsername_matchesOriginalInput() {
        String token = jwtUtil.generateToken("jane", "CUSTOMER", 1L);

        String extracted = jwtUtil.extractUsername(token);

        assertEquals("jane", extracted);
    }

    // Test 13: Validate expired JWT token -> isTokenExpired returns true
    @Test
    void isTokenExpired_withExpiredToken_returnsTrue() {
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", -1000L); // already expired
        String token = jwtUtil.generateToken("jane", "CUSTOMER", 1L);

        assertTrue(jwtUtil.isTokenExpired(token));
    }
}
