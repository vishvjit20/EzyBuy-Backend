package com.vj.ezybuy.users.service.impl;

import com.vj.ezybuy.users.dto.*;
import com.vj.ezybuy.users.entity.RefreshToken;
import com.vj.ezybuy.users.entity.Role;
import com.vj.ezybuy.users.entity.User;
import com.vj.ezybuy.users.exception.InvalidRequestException;
import com.vj.ezybuy.users.exception.ResourceNotFoundException;
import com.vj.ezybuy.users.repository.RefreshTokenRepository;
import com.vj.ezybuy.users.repository.UserRepository;
import com.vj.ezybuy.users.service.JwtService;
import com.vj.ezybuy.users.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new InvalidRequestException("Email already exists " +  userDto.getEmail());
        }
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setPhoneNumber(userDto.getPhoneNumber());
        user.setAddress(userDto.getAddress());
        user.setRole(Role.GUEST);
        User savedUser = userRepository.save(user);
        return toDto(savedUser);
    }

    @Override
    public UserDto getUserById(UUID id) {
        return toDto(getUser(id));
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not present with email: " + email));
        return toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(UUID id, UserDto userDto) {
        return null;
    }

    @Override
    public void deleteUser(UUID id) {
        userRepository.delete(getUser(id));
    }

    @Override
    public void changeUserRole(UUID id, Role role) {
        User user = getUser(id);
        user.setRole(role);
        userRepository.save(user);
    }

    private User getUser(UUID id) {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        log.info("Login service started:");
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new InvalidRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid email or password");
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        var refreshTokenObj = new RefreshToken();
        refreshTokenObj.setRefreshToken(refreshToken);
        refreshTokenObj.setActive(true);
        refreshTokenObj.setUser(user);
        refreshTokenRepository.save(refreshTokenObj);

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(accessToken);
        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setUser(toDto(user));

        log.info("Login service executed:");

        return loginResponse;
    }

    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest refreshRequest) {
        String refreshToken = refreshRequest.getRefreshToken();
        String email = jwtService.extractUsername(refreshToken);

        if (!jwtService.getTokenType(refreshToken).equals("refresh_token")) {
            throw new InvalidRequestException("Invalid refresh token");
        }

        RefreshToken refreshTokenObj = refreshTokenRepository
                .findByRefreshToken(refreshToken)
                .orElseThrow(() -> new InvalidRequestException("Invalid refresh token"));

        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found for the given refresh token"));

        if (!refreshTokenObj.getActive()) {
            throw new InvalidRequestException("Invalid refresh token");
        }

        if (!jwtService.isTokenValid(refreshToken, user.getEmail())) {
            throw new InvalidRequestException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtService.generateRefreshToken(user.getEmail());

        refreshTokenObj.setActive(false);
        refreshTokenRepository.save(refreshTokenObj);

        var refreshTokenObj2 = new RefreshToken();
        refreshTokenObj2.setRefreshToken(newRefreshToken);
        refreshTokenObj2.setActive(true);
        refreshTokenObj2.setUser(user);
        refreshTokenRepository.save(refreshTokenObj2);

        TokenRefreshResponse refreshResponse = new TokenRefreshResponse();
        refreshResponse.setAccessToken(newAccessToken);
        refreshResponse.setRefreshToken(newRefreshToken);

        return refreshResponse;
    }

    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setAddress(user.getAddress());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
