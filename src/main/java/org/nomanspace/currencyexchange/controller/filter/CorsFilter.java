package org.nomanspace.currencyexchange.controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.UUID;

@WebFilter("/*")
public class CorsFilter implements Filter {

    private static final String[] allowedOrigins = {"http://localhost:80", "http://127.0.0.1:80"};


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        MDC.put("requestId", UUID.randomUUID().toString());
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        //получить ориджин из заголовков
        /*String requestOrigin = httpServletRequest.getHeader("Origin");
        if (isAllowedOrigin(requestOrigin)) {
            resp.addHeader("Access-Control-Allow-Origin",
                    requestOrigin);
            resp.addHeader("Access-Control-Allow-Headers",
                    "*");
            resp.addHeader("Access-Control-Allow-Methods",
                    "GET, POST, PATCH, OPTIONS");


            if (((HttpServletRequest) request).getMethod().equals("OPTIONS")) {
                resp.setStatus(HttpServletResponse.SC_OK);
                return;
            }
        }*/

        String requestOrigin = httpServletRequest.getHeader("Origin");
        if (requestOrigin != null) {
            resp.setHeader("Access-Control-Allow-Origin",
                    requestOrigin);
            resp.setHeader("Access-Control-Allow-Methods",
                    "GET, POST, PATCH, OPTIONS");
            resp.setHeader("Access-Control-Allow-Headers",
                    "Content-Type");
            resp.setHeader("Access-Control-Max-Age",
                    "3600");
            if (httpServletRequest.getMethod().equals("OPTIONS")) {
                resp.setStatus(HttpServletResponse.SC_OK);
                return;
            }
        }


        chain.doFilter(request, response);
        MDC.remove("requestId");
    }

    private boolean isAllowedOrigin(String origin) {
        for (String allowedOrigin : allowedOrigins) {
            if (allowedOrigin.equals(origin)) {
                return true;
            }
        }
        return false;
    }
}
