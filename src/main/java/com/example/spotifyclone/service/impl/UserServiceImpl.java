package com.example.spotifyclone.service.impl;

import com.example.spotifyclone.dto.ErrorDto;
import com.example.spotifyclone.dto.RegisterRequest;
import com.example.spotifyclone.dto.RegisterResponse;
import com.example.spotifyclone.enums.ROLE;
import com.example.spotifyclone.mapper.UserMapper;
import com.example.spotifyclone.repository.UserRepository;
import com.example.spotifyclone.service.EmailService;
import com.example.spotifyclone.service.UserService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /*@Override
    public RegisterResponse register(RegisterRequest req) {

        /*if(userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email is already registered!");
        }*//*

        if(userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException();
        }

        var user = userMapper.toEntity(req);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(ROLE.USER);

        userRepository.save(user);
        return userMapper.toResponse(user);
    }*/


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

        var savedUser = userRepository.save(user);

        try {
            emailService.sendPostRegisterEmail(savedUser.getEmail(), savedUser.getName());
        } catch (MessagingException e) {
            log.warn("Failed to send registration email to {}: {}", savedUser.getEmail(), e.getMessage());
        }


        return userMapper.toResponse(savedUser);
    }
}
