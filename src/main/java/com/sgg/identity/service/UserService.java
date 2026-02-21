package com.sgg.identity.service;

import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.identity.dto.AuthSyncRequest;
import com.sgg.identity.dto.UpdateProfileRequest;
import com.sgg.identity.dto.UserProfileDto;
import com.sgg.identity.entity.User;
import com.sgg.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * Sincroniza el usuario de Supabase con la base de datos local.
     * Se llama en el primer login (o cada login para mantener datos actualizados).
     */
    public UserProfileDto syncUser(AuthSyncRequest request) {
        User user = userRepository.findBySupabaseUid(request.getSupabaseUid())
                .orElseGet(() -> {
                    log.info("Creando usuario local para supabase_uid={}", request.getSupabaseUid());
                    User newUser = new User();
                    newUser.setSupabaseUid(request.getSupabaseUid());
                    return newUser;
                });

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setAvatarUrl(request.getAvatarUrl());

        return toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toDto(user);
    }

    public UserProfileDto updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        return toDto(userRepository.save(user));
    }

    private UserProfileDto toDto(User user) {
        return new UserProfileDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getCreatedAt()
        );
    }
}
