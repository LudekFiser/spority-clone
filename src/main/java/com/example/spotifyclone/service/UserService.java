package com.example.spotifyclone.service;

import com.example.spotifyclone.dto.RegisterRequest;
import com.example.spotifyclone.dto.RegisterResponse;

public interface UserService {

    RegisterResponse register(RegisterRequest req);
}
