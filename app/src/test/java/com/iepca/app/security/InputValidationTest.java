package com.iepca.app.security;

import org.junit.Test;
import static org.junit.Assert.*;

public class InputValidationTest {

    @Test
    public void shouldDetectSqlInjectionInEmail() {
        String malicious = "' OR 1=1 --";
        assertTrue(containsSqlInjectionPattern(malicious));
    }

    @Test
    public void shouldDetectXssInInput() {
        String malicious = "<script>alert('xss')</script>";
        assertTrue(containsXssPattern(malicious));
    }

    @Test
    public void shouldAcceptValidEmail() {
        String valid = "docente@iepca.edu.pe";
        assertFalse(containsSqlInjectionPattern(valid));
        assertFalse(containsXssPattern(valid));
        assertTrue(isValidEmail(valid));
    }

    @Test
    public void shouldRejectEmailWithoutAt() {
        assertFalse(isValidEmail("invalidemail.com"));
    }

    @Test
    public void shouldRejectEmptyEmail() {
        assertFalse(isValidEmail(""));
        assertFalse(isValidEmail(null));
    }

    @Test
    public void shouldDetectUnionBasedSqlInjection() {
        assertTrue(containsSqlInjectionPattern("admin' UNION SELECT * FROM users--"));
    }

    @Test
    public void shouldDetectDropTableInjection() {
        assertTrue(containsSqlInjectionPattern("'; DROP TABLE users;--"));
    }

    @Test
    public void shouldDetectImgTagXss() {
        assertTrue(containsXssPattern("<img src=x onerror=alert(1)>"));
    }

    @Test
    public void shouldAcceptNormalSpanishText() {
        assertFalse(containsSqlInjectionPattern("Maria Elena Tasa Catanzaro"));
        assertFalse(containsXssPattern("Maria Elena Tasa Catanzaro"));
    }

    @Test
    public void shouldValidateDniFormat() {
        assertTrue(isValidDni("70000001"));
        assertFalse(isValidDni("123"));
        assertFalse(isValidDni("ABCDEFGH"));
        assertFalse(isValidDni(null));
        assertFalse(isValidDni(""));
    }

    @Test
    public void shouldValidatePasswordStrength() {
        assertTrue(isStrongPassword("SecurePass123"));
        assertFalse(isStrongPassword("123"));
        assertFalse(isStrongPassword(""));
        assertFalse(isStrongPassword(null));
    }

    private boolean containsSqlInjectionPattern(String input) {
        if (input == null) return false;
        String lower = input.toLowerCase();
        return lower.contains("' or ") || lower.contains("union select")
                || lower.contains("drop table") || lower.contains("'; ")
                || lower.contains("1=1") || lower.contains("--");
    }

    private boolean containsXssPattern(String input) {
        if (input == null) return false;
        String lower = input.toLowerCase();
        return lower.contains("<script") || lower.contains("onerror=")
                || lower.contains("javascript:") || lower.contains("<img ");
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return email.matches("^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
    }

    private boolean isValidDni(String dni) {
        if (dni == null || dni.isEmpty()) return false;
        return dni.matches("^\\d{8}$");
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 6) return false;
        return password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }
}
