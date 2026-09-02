package com.vj.ezybuy.users.service;

import com.vj.ezybuy.users.dto.*;
import com.vj.ezybuy.users.entity.Role;

import java.util.List;
import java.util.UUID;


public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto getUserById(UUID id);

    UserDto getUserByEmail(String email);

    List<UserDto> getAllUsers();

    UserDto updateUser(UUID id, UserDto userDto);

    void deleteUser(UUID id);

    void changeUserRole(UUID id, Role role);

    LoginResponse login(LoginRequest loginRequest);

    TokenRefreshResponse refreshToken(TokenRefreshRequest refreshRequest);
}
