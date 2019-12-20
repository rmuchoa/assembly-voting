package com.cooperative.assembly.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static java.util.Optional.of;
import static com.cooperative.assembly.user.VotingAbility.ABLE_TO_VOTE;

@Component
public class UserInfoClient {

    public Optional<UserInfo> getUserInfo(final String userId) {
        UserInfo userInfo = new UserInfo(ABLE_TO_VOTE);
        return of(userInfo);
    }

}
