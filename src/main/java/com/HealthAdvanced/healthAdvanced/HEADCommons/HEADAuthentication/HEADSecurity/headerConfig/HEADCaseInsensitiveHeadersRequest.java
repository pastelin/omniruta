package com.HealthAdvanced.healthAdvanced.HEADCommons.HEADAuthentication.HEADSecurity.headerConfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.*;
import java.util.stream.Collectors;

public class HEADCaseInsensitiveHeadersRequest extends HttpServletRequestWrapper {
    private final Map<String, List<String>> headersCI;

    public HEADCaseInsensitiveHeadersRequest(HttpServletRequest req) {
        super(req);
        this.headersCI = Collections.list(req.getHeaderNames()).stream()
                .collect(Collectors.toMap(
                        h -> h, req::getHeaders,
                        (a,b) -> a,
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
                )).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> Collections.list(e.getValue()),
                        (a,b) -> a,
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
                ));
    }

    @Override public String getHeader(String name) {
        var list = headersCI.get(name);
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    @Override public Enumeration<String> getHeaders(String name) {
        var list = headersCI.getOrDefault(name, List.of());
        return Collections.enumeration(list);
    }

    @Override public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(headersCI.keySet());
    }
}
