package com.cooperative.assembly.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Log4j2
@Component
public class RestClient {

    @Autowired
    private RestTemplate restTemplate;

    private <T> ResponseEntity<T> performRest(URI uri, HttpMethod method, HttpEntity<?> httpEntity, Class<T> returnTypeClass) throws HttpClientErrorException {

        ResponseEntity<T> response = null;
        try {

            response = restTemplate.exchange(uri, method, httpEntity, returnTypeClass);

        } catch (HttpClientErrorException e) {
            String msg = "Exception while performing REST service call: " + e.getMessage();
            log.error(msg);
            throw e;
        }

        return response;

    }

    public <T> ResponseEntity<T> get(URI uri, HttpEntity<?> httpEntity, Class<T> returnType) throws HttpClientErrorException {
        return performRest(uri, HttpMethod.GET, httpEntity, returnType);
    }
}
