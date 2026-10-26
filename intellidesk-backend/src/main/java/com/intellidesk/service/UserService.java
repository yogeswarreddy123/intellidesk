package com.intellidesk.service;

import com.intellidesk.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    AuthResponse authenticate(AuthRequest request);

    UserDto register(RegisterRequest request);

    UserDto getUserById(Long id);

    UserDto getUserByEmail(String email);

    UserDto getCurrentUserProfile();

    Page<UserDto> getAllUsers(String search, Pageable pageable);

    List<UserDto> getActiveAgents();

    UserDto createUser(UserCreateUpdateDto dto);

    UserDto updateUser(Long id, UserCreateUpdateDto dto);

    UserDto toggleUserStatus(Long id);

    void deleteUser(Long id);
}
