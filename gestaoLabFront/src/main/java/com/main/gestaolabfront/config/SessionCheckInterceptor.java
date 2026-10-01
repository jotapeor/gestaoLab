package com.main.gestaolabfront.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionCheckInterceptor implements HandlerInterceptor {

    private static final String[] PRIMEIRO_ACESSO_BYPASS = {"/trocar-senha", "/fazer-trocar-senha", "/logout"};

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String servletPath = request.getServletPath();
        String path = (servletPath != null && !servletPath.isEmpty()) ? servletPath : request.getRequestURI();

        HttpSession session = request.getSession(false);
        String token = (session != null) ? (String) session.getAttribute("token") : null;

        if (token == null) {
            response.sendRedirect("/login");
            return false;
        }

        for (String bypass : PRIMEIRO_ACESSO_BYPASS) {
            if (path.startsWith(bypass)) return true;
        }

        String primeiroAcesso = (String) session.getAttribute("primeiroAcesso");
        if ("true".equals(primeiroAcesso)) {
            response.sendRedirect("/trocar-senha");
            return false;
        }

        return true;
    }
}
