package com.jaha.server.emaul.config;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.google.common.collect.Lists;
import com.jaha.server.emaul.util.SessionAttrs;

public class LoginChecker extends HandlerInterceptorAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginChecker.class);

    List<String> startWithFilterUrls = Lists.newArrayList("/api/public", "/api/parcel", "/health", "/error", "/v2/api/public", "/v2/api/board/common");

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURI();
        LOGGER.debug("<<login checker. req url>> {}", url);

        if (!isLoginChecked(request)) {
            boolean invalidSession = true;

            for (String filterUrl : startWithFilterUrls) {
                if (url.startsWith(filterUrl)) {
                    invalidSession = false;
                    break;
                }
            }

            if (invalidSession) {
                PrintWriter writer = response.getWriter();
                writer.print("INVALID_SESSION");
                writer.close();

                return false;
            }
        }

        return super.preHandle(request, response, handler);
    }

    private boolean isLoginChecked(HttpServletRequest request) {
        HttpSession session = request.getSession();

        Long userId = SessionAttrs.getUserId(session);

        return userId != null && userId != 0l;
    }

}
