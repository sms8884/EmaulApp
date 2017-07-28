package com.jaha.server.emaul.util;

import javax.servlet.http.HttpSession;

/**
 * Created by doring on 15. 4. 14..
 */
public class SessionAttrs {

    public static Long getUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            session.invalidate();
        }
        return userId;
    }

    public static void setUserId(HttpSession session, Long userId) {
        session.setAttribute("userId", userId);
    }

    // 접속 경로(android/ios) 추가
    public static String getKind(HttpSession session) {
        String kind = (String) session.getAttribute("kind");
        if (kind == null) {
            session.invalidate();
        }
        return kind;
    }

    public static void setKind(HttpSession session, String kind) {
        session.setAttribute("kind", kind);
    }

}
