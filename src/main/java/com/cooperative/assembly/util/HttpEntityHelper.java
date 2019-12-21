package com.cooperative.assembly.util;

import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class HttpEntityHelper {

    public <T> HttpEntity<T> getEntityWithHeaderAndBody(T body, String key, String value) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(key, value);
        return new HttpEntity<T>(body, headers);
    }

}
