package com.ariscend.backend.service;

import com.ariscend.backend.dto.user.CreateUserRequest;
import com.ariscend.backend.dto.user.UserResponse;
import com.ariscend.backend.entity.AppUser;
import com.ariscend.backend.repository.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public List<UserResponse> getAll() {
        return appUserRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse create(CreateUserRequest request) {
        String name = request.getName().trim();
        String email = request.getEmail().trim().toLowerCase();
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Ese correo ya está registrado.");
        }

        AppUser user = new AppUser();
        user.setName(name);
        user.setEmail(email);

        return UserResponse.from(appUserRepository.save(user));
    }
}
