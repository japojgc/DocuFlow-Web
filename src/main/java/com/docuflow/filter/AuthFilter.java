package com.docuflow.filter;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String uri = req.getRequestURI();
        String contextPath = req.getContextPath();

        HttpSession session = req.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("user") != null);

        boolean isLoginRoute = uri.endsWith("/login") || uri.endsWith("/login.jsp");
        boolean isRootRoute = uri.equals(contextPath + "/") || uri.equals(contextPath);

        if (loggedIn) {
            if (isLoginRoute || isRootRoute) {
                res.sendRedirect(contextPath + "/upload");
            } else {
                chain.doFilter(request, response);
            }
        } else {
            if (isLoginRoute) {
                chain.doFilter(request, response);
            } else {
                res.sendRedirect(contextPath + "/login");
            }
        }
    }
}