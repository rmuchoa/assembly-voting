package com.cooperative.assembly.user;

import com.cooperative.assembly.error.exception.NotFoundReferenceException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Log4j2
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
            log.error("None user information was found on user-info api for CPF: ", userId);
            throw new NotFoundReferenceException("User", "user.not.found");
        }

        UserInfo user = userInfo.get();
        log.debug("Found user information on user-info api", user);
        return new User(userId, user.getStatus());
    }

}
