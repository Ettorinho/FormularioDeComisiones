package com.comisiones.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuditoriaService helper methods.
 * These tests do not require a database connection.
 */
public class AuditoriaServiceTest {

    // -------------------------------------------------------------------------
    // getClientIp tests
    // -------------------------------------------------------------------------

    @Test
    void getClientIp_returnsRemoteAddr_whenNoForwardedHeader() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.10");

        assertEquals("192.168.1.10", AuditoriaService.getClientIp(request));
    }

    @Test
    void getClientIp_returnsFirstIp_whenForwardedChainPresent() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("10.0.0.1, 172.16.0.1, 192.168.0.1");

        assertEquals("10.0.0.1", AuditoriaService.getClientIp(request));
    }

    @Test
    void getClientIp_trimsWhitespace_fromForwardedIp() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("  10.0.0.5  , 172.16.0.1");

        assertEquals("10.0.0.5", AuditoriaService.getClientIp(request));
    }

    @Test
    void getClientIp_returnsNull_whenRequestIsNull() {
        assertNull(AuditoriaService.getClientIp(null));
    }

    @Test
    void getClientIp_returnsRemoteAddr_whenForwardedHeaderIsEmpty() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn("10.1.2.3");

        assertEquals("10.1.2.3", AuditoriaService.getClientIp(request));
    }

    // -------------------------------------------------------------------------
    // getUserAgent tests
    // -------------------------------------------------------------------------

    @Test
    void getUserAgent_returnsHeader_whenPresent() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

        assertEquals("Mozilla/5.0", AuditoriaService.getUserAgent(request));
    }

    @Test
    void getUserAgent_truncatesAt500Chars() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String longUa = "A".repeat(600);
        when(request.getHeader("User-Agent")).thenReturn(longUa);

        String result = AuditoriaService.getUserAgent(request);
        assertNotNull(result);
        assertEquals(500, result.length());
    }

    @Test
    void getUserAgent_returnsNull_whenHeaderAbsent() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getHeader("User-Agent")).thenReturn(null);

        assertNull(AuditoriaService.getUserAgent(request));
    }

    @Test
    void getUserAgent_returnsNull_whenRequestIsNull() {
        assertNull(AuditoriaService.getUserAgent(null));
    }

    @Test
    void getUserAgent_doesNotTruncate_when500CharsExact() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        String ua = "B".repeat(500);
        when(request.getHeader("User-Agent")).thenReturn(ua);

        assertEquals(500, AuditoriaService.getUserAgent(request).length());
    }
}
