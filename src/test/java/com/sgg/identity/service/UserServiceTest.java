package com.sgg.identity.service;

import com.sgg.common.exception.ResourceNotFoundException;
import com.sgg.identity.dto.AuthSyncRequest;
import com.sgg.identity.dto.UpdateProfileRequest;
import com.sgg.identity.dto.UserProfileDto;
import com.sgg.identity.entity.User;
import com.sgg.identity.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setSupabaseUid("uid-abc");
        existingUser.setFullName("John Doe");
        existingUser.setEmail("john@example.com");
        existingUser.setAvatarUrl("http://avatar.com/john.jpg");
        existingUser.setCreatedAt(LocalDateTime.now());
    }

    // --- syncUser ---

    @Test
    void syncUser_createsNewUser_whenNotExists() {
        AuthSyncRequest request = new AuthSyncRequest();
        request.setSupabaseUid("uid-new");
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");

        when(userRepository.findBySupabaseUid("uid-new")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(2L);
            u.setCreatedAt(LocalDateTime.now());
            return u;
        });

        UserProfileDto result = userService.syncUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Jane Doe");
        assertThat(result.getEmail()).isEqualTo("jane@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void syncUser_updatesExistingUser_whenAlreadyExists() {
        AuthSyncRequest request = new AuthSyncRequest();
        request.setSupabaseUid("uid-abc");
        request.setFullName("John Updated");
        request.setEmail("john@example.com");

        when(userRepository.findBySupabaseUid("uid-abc")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        UserProfileDto result = userService.syncUser(request);

        assertThat(result).isNotNull();
        verify(userRepository, never()).save(argThat(u -> u.getId() == null)); // no crea uno nuevo
        verify(userRepository).save(existingUser);
    }

    // --- getProfile ---

    @Test
    void getProfile_returnsDto_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        UserProfileDto result = userService.getProfile(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void getProfile_throwsResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // --- updateProfile ---

    @Test
    void updateProfile_updatesNameAndAvatar() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("New Name");
        request.setAvatarUrl("http://new-avatar.com/img.jpg");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        UserProfileDto result = userService.updateProfile(1L, request);

        assertThat(result).isNotNull();
        verify(userRepository).save(existingUser);
    }

    @Test
    void updateProfile_throwsResourceNotFoundException_whenUserNotFound() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFullName("Name");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
