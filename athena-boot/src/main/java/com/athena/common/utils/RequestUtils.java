package com.athena.common.utils;

import com.google.common.collect.Maps;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author Mr.sun
 * @date 2021/12/15 17:18
 * @description
 */
public class RequestUtils {
    public static Map<String, String> getHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String,String> map = Maps.newHashMap();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            map.put(key, request.getHeader(key));
        }
        return map;
    }
    public static Map<String, String> getHeaders(HttpServletResponse response) {
        Collection<String> headerNames = response.getHeaderNames();
        Map<String,String> map = Maps.newHashMap();
        headerNames.forEach(headerName -> map.put(headerName, response.getHeader(headerName)));
        return map;
    }
}
