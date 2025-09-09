package com.moviebooking.user.application.service;

import com.moviebooking.user.application.dto.LoginRequest;
import com.moviebooking.user.application.dto.RegisterRequest;
import com.moviebooking.user.domain.User;
import com.moviebooking.user.infrastructure.repository.UserRepository;
import com.moviebooking.user.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Test User");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");
        registerRequest.setPhone("1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password("encoded_password")
                .phone(registerRequest.getPhone())
                .roles(Set.of("USER"))
                .build();
    }

    @Test
    void register_ShouldCreateNewUser() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.createToken(any(User.class))).thenReturn("test_token");

        // When
        var result = authService.register(registerRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("test_token");
        assertThat(result.getEmail()).isEqualTo(registerRequest.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Then
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Email is already registered");
        
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldAuthenticateUser() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.createToken(user)).thenReturn("test_token");

        // When
        var result = authService.login(loginRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("test_token");
        assertThat(result.getEmail()).isEqualTo(loginRequest.getEmail());
        verify(userRepository).save(user);
    }
}
