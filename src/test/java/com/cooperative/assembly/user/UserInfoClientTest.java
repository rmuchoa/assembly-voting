package com.cooperative.assembly.user;

import com.cooperative.assembly.util.HttpEntityHelper;
import com.cooperative.assembly.util.RestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static com.cooperative.assembly.user.VotingAbility.ABLE_TO_VOTE;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { UserInfoClient.class, RestClient.class, HttpEntityHelper.class })
@TestPropertySource(properties = {
        "api.user.info.server=http://localhost:8080",
        "api.user.info.url={server}/users/{cpf}"
})
public class UserInfoClientTest {

    @Autowired
    private UserInfoClient client;

    @MockBean
    private RestClient restClient;

    @Autowired
    private HttpEntityHelper entityHelper;

    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    @Captor ArgumentCaptor<HttpEntity> httpEntityCaptor;

    private String userId;

    @Before
    public void setUp() {
        this.userId = "30952418010";
    }

    @Test
    public void shouldBuildHttpEntityAndExchangeGetUserInfoRequestByRestClientForUserId() {
        ResponseEntity response = ResponseEntity.ok(new UserInfo(ABLE_TO_VOTE));
        when(restClient.get(any(URI.class), any(HttpEntity.class), eq(UserInfo.class))).thenReturn(response);

        client.getUserInfo(userId);

        verify(restClient, only()).get(any(URI.class), any(HttpEntity.class), eq(UserInfo.class));
    }

    @Test
    public void shouldCheckIfUriHasUserIdParameterWhenExchangeGetUserInfoRequestByRestClientForUrlAndHttpEntity() {
        ResponseEntity response = ResponseEntity.ok(new UserInfo(ABLE_TO_VOTE));
        when(restClient.get(any(URI.class), any(HttpEntity.class), eq(UserInfo.class))).thenReturn(response);

        client.getUserInfo(userId);

        verify(restClient, only()).get(uriCaptor.capture(), any(HttpEntity.class), eq(UserInfo.class));
        assertThat(uriCaptor.getValue().getPath(), containsString(userId));
    }

    @Test
    public void shouldCheckIfHttpEntityParameterHasEmptyBodyAndContentTypeHeaderWhenExchangeGetUserInfoRequestByRestClientForUrlAndHttpEntity() {
        ResponseEntity response = ResponseEntity.ok(new UserInfo(ABLE_TO_VOTE));
        when(restClient.get(any(URI.class), any(HttpEntity.class), eq(UserInfo.class))).thenReturn(response);

        client.getUserInfo(userId);

        verify(restClient, only()).get(any(URI.class), httpEntityCaptor.capture(), eq(UserInfo.class));
        assertThat(httpEntityCaptor.getValue().getBody(), equalTo(""));
        assertThat(httpEntityCaptor.getValue().getHeaders().containsKey("Content-Type"), is(true));
        assertThat(httpEntityCaptor.getValue().getHeaders().get("Content-Type").get(0), equalTo("application/json"));
    }

    @Test
    public void shouldReturnUserInfoResponseWhenExchangeGetUserInfoRequestByRestClientForUrlAndHttpEntity() {
        UserInfo expectedUserInfo = new UserInfo(ABLE_TO_VOTE);
        ResponseEntity response = ResponseEntity.ok(expectedUserInfo);
        when(restClient.get(any(URI.class), any(HttpEntity.class), eq(UserInfo.class))).thenReturn(response);

        Optional<UserInfo> userInfo = client.getUserInfo(userId);

        assertThat(userInfo.isPresent(), is(true));
        assertThat(userInfo.get(), is(expectedUserInfo));
        assertThat(userInfo.get(), hasProperty("status", equalTo(ABLE_TO_VOTE)));
    }

}
