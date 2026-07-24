package com.ariscend.backend.dto.user;

import com.ariscend.backend.entity.AppUser;

public class UserResponse {

    private Long id;
    private String name;
    private String email;

    public static UserResponse from(AppUser user) {
        UserResponse response = new UserResponse();
        response.id = user.getId();
        response.name = user.getName();
        response.email = user.getEmail();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}