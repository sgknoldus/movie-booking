package com.moviebooking.user.application.service;

import com.moviebooking.user.application.dto.UpdateProfileRequest;
import com.moviebooking.user.application.dto.UserProfileResponse;
import com.moviebooking.user.domain.User;
import com.moviebooking.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID testUserId;
    private UpdateProfileRequest updateProfileRequest;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = User.builder()
                .id(testUserId)
                .name("Test User")
                .email("test@example.com")
                .phone("1234567890")
                .roles(Set.of("USER"))
                .createdAt(LocalDateTime.now().minusDays(1))
                .lastLoginAt(LocalDateTime.now())
                .build();

        updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setName("Updated Name");
        updateProfileRequest.setPhone("9876543210");
    }

    @Test
    void getUserProfile_ShouldReturnUserProfile_WhenUserExists() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // When
        UserProfileResponse result = userService.getUserProfile(testUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUserId);
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getPhone()).isEqualTo("1234567890");
        assertThat(result.getRoles()).containsExactly("USER");
        verify(userRepository).findById(testUserId);
    }

    @Test
    void getUserProfile_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> userService.getUserProfile(testUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: " + testUserId);
        verify(userRepository).findById(testUserId);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        User user2 = User.builder()
                .id(UUID.randomUUID())
                .name("User 2")
                .email("user2@example.com")
                .phone("5555555555")
                .roles(Set.of("USER"))
                .build();

        when(userRepository.findAll()).thenReturn(List.of(testUser, user2));

        // When
        List<UserProfileResponse> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Test User");
        assertThat(result.get(1).getName()).isEqualTo("User 2");
        verify(userRepository).findAll();
    }

    @Test
    void updateProfile_ShouldUpdateUserProfile_WhenUserExists() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserProfileResponse result = userService.updateProfile(testUserId, updateProfileRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.getName()).isEqualTo("Updated Name");
        assertThat(testUser.getPhone()).isEqualTo("9876543210");
        verify(userRepository).findById(testUserId);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfile_ShouldUpdateOnlyName_WhenPhoneIsNull() {
        // Given
        updateProfileRequest.setPhone(null);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserProfileResponse result = userService.updateProfile(testUserId, updateProfileRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(testUser.getName()).isEqualTo("Updated Name");
        assertThat(testUser.getPhone()).isEqualTo("1234567890"); // Original phone retained
        verify(userRepository).save(testUser);
    }

    @Test
    void updateProfile_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> userService.updateProfile(testUserId, updateProfileRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: " + testUserId);
        verify(userRepository).findById(testUserId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Given
        when(userRepository.existsById(testUserId)).thenReturn(true);

        // When
        userService.deleteUser(testUserId);

        // Then
        verify(userRepository).existsById(testUserId);
        verify(userRepository).deleteById(testUserId);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.existsById(testUserId)).thenReturn(false);

        // Then
        assertThatThrownBy(() -> userService.deleteUser(testUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User not found with id: " + testUserId);
        verify(userRepository).existsById(testUserId);
        verify(userRepository, never()).deleteById(any());
    }
}