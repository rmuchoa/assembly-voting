package com.cooperative.assembly.user;

import com.cooperative.assembly.util.HttpEntityHelper;
import com.cooperative.assembly.util.RestClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

@Log4j2
@Component
public class UserInfoClient {

    @Value("${api.user.info.server}")
    private String server;

    @Value("${api.user.info.url}")
    private String url;

    protected HttpEntityHelper entityHelper;
    private RestClient client;

    public UserInfoClient(final HttpEntityHelper entityHelper, final RestClient client) {
        this.entityHelper = entityHelper;
        this.client = client;
    }

    public Optional<UserInfo> getUserInfo(final String userId) {
        try {

            String url = this.url
                    .replace("{server}", server)
                    .replace("{cpf}", userId);

            log.debug("Getting user information by rest client on URL: ", url);
            UserInfo userInfo = requestUserInfo(url, userId);
            return of(userInfo);

        } catch (HttpClientErrorException ex) {
            return empty();
        }
    }

    protected UserInfo requestUserInfo(final String url, final String userId) {

        URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .build().encode().toUri();

        HttpEntity<String> httpEntity = entityHelper.getEntityWithHeaderAndBody("", "Content-Type", "application/json");
        ResponseEntity<UserInfo> response = client.get(uri, httpEntity, UserInfo.class);
        return response.getBody();

    }

}
