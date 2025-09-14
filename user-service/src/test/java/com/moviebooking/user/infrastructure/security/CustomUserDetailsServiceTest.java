package com.moviebooking.user.infrastructure.security;

import com.moviebooking.user.domain.User;
import com.moviebooking.user.infrastructure.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email(testEmail)
                .password("encodedPassword123")
                .phone("1234567890")
                .roles(Set.of("USER", "ADMIN"))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(testEmail);
        assertThat(result.getPassword()).isEqualTo("encodedPassword123");
        assertThat(result.getAuthorities()).hasSize(2);
        assertThat(result.isEnabled()).isTrue();
        assertThat(result.isAccountNonExpired()).isTrue();
        assertThat(result.isAccountNonLocked()).isTrue();
        assertThat(result.isCredentialsNonExpired()).isTrue();

        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(testEmail))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: " + testEmail);

        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void loadUserByUsername_ShouldHandleNullEmail() {
        // Given
        String nullEmail = null;
        when(userRepository.findByEmail(nullEmail)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(nullEmail))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: " + nullEmail);

        verify(userRepository).findByEmail(nullEmail);
    }

    @Test
    void loadUserByUsername_ShouldHandleEmptyEmail() {
        // Given
        String emptyEmail = "";
        when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(emptyEmail))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found with email: " + emptyEmail);

        verify(userRepository).findByEmail(emptyEmail);
    }

    @Test
    void loadUserByUsername_ShouldReturnCorrectAuthorities() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername(testEmail);

        // Then
        assertThat(result.getAuthorities())
                .hasSize(2)
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_ShouldHandleUserWithSingleRole() {
        // Given
        User singleRoleUser = User.builder()
                .id(UUID.randomUUID())
                .name("Single Role User")
                .email("single@example.com")
                .password("password")
                .phone("1111111111")
                .roles(Set.of("USER"))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();

        when(userRepository.findByEmail("single@example.com")).thenReturn(Optional.of(singleRoleUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername("single@example.com");

        // Then
        assertThat(result.getAuthorities())
                .hasSize(1)
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void loadUserByUsername_ShouldHandleUserWithNoRoles() {
        // Given
        User noRoleUser = User.builder()
                .id(UUID.randomUUID())
                .name("No Role User")
                .email("norole@example.com")
                .password("password")
                .phone("2222222222")
                .roles(Set.of())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();

        when(userRepository.findByEmail("norole@example.com")).thenReturn(Optional.of(noRoleUser));

        // When
        UserDetails result = customUserDetailsService.loadUserByUsername("norole@example.com");

        // Then
        assertThat(result.getAuthorities()).isEmpty();
    }
}