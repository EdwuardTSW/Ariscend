package com.ariscend.backend.controller;

import com.ariscend.backend.dto.user.CreateUserRequest;
import com.ariscend.backend.dto.user.UserResponse;
import com.ariscend.backend.service.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return appUserService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody CreateUserRequest request) {
        return appUserService.create(request);
    }
}