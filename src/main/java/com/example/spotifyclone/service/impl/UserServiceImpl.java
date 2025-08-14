package com.example.spotifyclone.service.impl;

import com.example.spotifyclone.dto.RegisterRequest;
import com.example.spotifyclone.dto.RegisterResponse;
import com.example.spotifyclone.enums.ROLE;
import com.example.spotifyclone.mapper.UserMapper;
import com.example.spotifyclone.repository.UserRepository;
import com.example.spotifyclone.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public RegisterResponse register(RegisterRequest req) {

        /*if(userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email is already registered!");
        }*/

        if(userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException();
        }

        var user = userMapper.toEntity(req);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(ROLE.USER);

        userRepository.save(user);
        return userMapper.toResponse(user);
    }
}
