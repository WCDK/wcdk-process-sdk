package com.wcdk.process.config;

import com.wcdk.process.WcdkProcessConnectionConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @auther WCDK
 * @date 2026/7/16
 * @version 1.0
 **/
public class WcdkProcessAuthInterceptor implements HandlerInterceptor {

    private static final String AUTH_HEADER = "WCDK_AUTH";

    private final WcdkProcessConnectionConfig connectionConfig;

    public WcdkProcessAuthInterceptor(WcdkProcessConnectionConfig connectionConfig) {
        this.connectionConfig = connectionConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!StringUtils.hasText(connectionConfig.getAuthFlg())) {
            return true;
        }
        String authValue = request.getHeader(AUTH_HEADER);
        if (connectionConfig.getAuthFlg().equals(authValue)) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"回调鉴权失败\",\"data\":null}");
        return false;
    }
}
