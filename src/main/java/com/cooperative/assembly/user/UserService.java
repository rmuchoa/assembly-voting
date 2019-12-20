package com.cooperative.assembly.user;

import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private UserInfoClient userInfoClient;

    @Autowired
    public UserService(final UserInfoClient userInfoClient) {
        this.userInfoClient = userInfoClient;
    }

    /**
     * Load user by userId getting user info ability status.
     *
     * @param userId
     * @return
     */
    public User loadUser(final String userId) {
        Optional<UserInfo> userInfo = userInfoClient.getUserInfo(userId);
        if (!userInfo.isPresent()) {
            throw new NotFoundReferenceException("User", "user.not.found");
        }

        UserInfo user = userInfo.get();
        return new User(userId, user.getStatus());
    }

}
