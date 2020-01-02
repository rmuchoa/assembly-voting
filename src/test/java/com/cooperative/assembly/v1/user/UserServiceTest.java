package com.cooperative.assembly.v1.user;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static com.cooperative.assembly.v1.user.VotingAbility.ABLE_TO_VOTE;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = UserService.class)
public class UserServiceTest {

    @Autowired
    private UserService service;

    @MockBean
    private UserInfoClient userInfoClient;

    private String userId;

    @Before
    public void setUp() {
        this.userId = "30952418010";
    }

    @Test
    public void shouldGetUserByCpfOnClientWhenLoadingUserById() {
        UserInfo userInfo = new UserInfo(ABLE_TO_VOTE);
        when(userInfoClient.getUserInfo(userId)).thenReturn(of(userInfo));

        service.loadUser(userId);

        verify(userInfoClient, only()).getUserInfo(userId);
    }

    @Test
    public void shouldReturnVotingAbilityWhenLoadUserIdentifiedByUserId() {
        UserInfo userInfo = new UserInfo(ABLE_TO_VOTE);
        when(userInfoClient.getUserInfo(userId)).thenReturn(of(userInfo));

        User user = service.loadUser(userId);

        assertThat(user, hasProperty("id", equalTo(userId)));
        assertThat(user, hasProperty("ability", equalTo(ABLE_TO_VOTE)));
    }

}
