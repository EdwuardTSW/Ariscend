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
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("El correo es obligatorio.");
        }

        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Ese correo ya está registrado.");
        }

        AppUser user = new AppUser();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        return UserResponse.from(appUserRepository.save(user));
    }
}