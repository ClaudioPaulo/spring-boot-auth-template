package com.claudiopaulo.userapp.service;

import com.claudiopaulo.userapp.dto.RegisterRequest;
import com.claudiopaulo.userapp.dto.UserResponse;
import com.claudiopaulo.userapp.entity.User;

import java.util.List;

public interface UserService {
    User registerUser(RegisterRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, RegisterRequest request);
    void deleteUser(Long id);
    User findByUsername(String username);
}
