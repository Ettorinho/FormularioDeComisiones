package com.comisiones.filter;

import com.comisiones.security.CsrfTokenUtil;
import com.comisiones.util.AppLogger;
import com.comisiones.util.StaticResourceUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CsrfFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // Sin inicialización
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;
        String contextPath = httpReq.getContextPath();
        String requestUri = httpReq.getRequestURI();
        String path = requestUri.substring(contextPath.length());

        if (StaticResourceUtil.esRecursoEstatico(path)) {
            chain.doFilter(request, response);
            return;
        }

        CsrfTokenUtil.exposeToken(httpReq);

        if ("POST".equalsIgnoreCase(httpReq.getMethod()) && !CsrfTokenUtil.isRequestTokenValid(httpReq)) {
            AppLogger.warn("Solicitud POST rechazada por token CSRF ausente o inválido: " + httpReq.getRequestURI());
            httpResp.sendError(HttpServletResponse.SC_FORBIDDEN, "Token CSRF ausente o inválido.");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Sin recursos que liberar
    }
}
