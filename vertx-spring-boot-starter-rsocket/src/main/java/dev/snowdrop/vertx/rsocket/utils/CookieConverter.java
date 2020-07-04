package dev.snowdrop.vertx.rsocket.utils;

import java.util.List;
import java.util.stream.Collectors;

import io.vertx.core.http.Cookie;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;

public final class CookieConverter {

    public static Cookie toCookie(ResponseCookie responseCookie) {
        Cookie cookie = Cookie.cookie(responseCookie.getName(), responseCookie.getValue())
            .setDomain(responseCookie.getDomain())
            .setPath(responseCookie.getPath())
            .setHttpOnly(responseCookie.isHttpOnly())
            .setSecure(responseCookie.isSecure());

        if (!responseCookie.getMaxAge().isNegative()) {
            cookie.setMaxAge(responseCookie.getMaxAge().getSeconds());
        }

        return cookie;
    }

    public static HttpCookie toHttpCookie(Cookie cookie) {
        return new HttpCookie(cookie.getName(), cookie.getValue());
    }

    public static List<ResponseCookie> toResponseCookies(String cookieHeader) {
        return java.net.HttpCookie.parse(cookieHeader)
            .stream()
            .map(CookieConverter::toResponseCookie)
            .collect(Collectors.toList());
    }

    private static ResponseCookie toResponseCookie(java.net.HttpCookie cookie) {
        return ResponseCookie.from(cookie.getName(), cookie.getValue())
            .domain(cookie.getDomain())
            .httpOnly(cookie.isHttpOnly())
            .maxAge(cookie.getMaxAge())
            .path(cookie.getPath())
            .secure(cookie.getSecure())
            .build();
    }

}
