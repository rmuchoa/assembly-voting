package com.cooperative.assembly.builder;

import com.cooperative.assembly.v1.user.User;

public class UserBuilder {

    public static GenericBuilder<User> get() {
        return GenericBuilder.of(() -> new User());
    }

}
