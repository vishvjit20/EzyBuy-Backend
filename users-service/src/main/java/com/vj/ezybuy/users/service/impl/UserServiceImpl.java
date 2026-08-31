package com.vj.ezybuy.users.service.impl;

import com.vj.ezybuy.users.dto.UserDto;
import com.vj.ezybuy.users.entity.Role;
import com.vj.ezybuy.users.entity.User;
import com.vj.ezybuy.users.exception.InvalidRequestException;
import com.vj.ezybuy.users.exception.ResourceNotFoundException;
import com.vj.ezybuy.users.repository.UserRepository;
import com.vj.ezybuy.users.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new InvalidRequestException("Email already exists " +  userDto.getEmail());
        }
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
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
