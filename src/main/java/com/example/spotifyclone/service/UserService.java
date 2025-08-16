package com.example.spotifyclone.service;

import com.example.spotifyclone.dto.ChangePasswordRequest;
import com.example.spotifyclone.dto.RegisterRequest;
import com.example.spotifyclone.dto.RegisterResponse;
import com.example.spotifyclone.dto.VerifyAccountRequest;

public interface UserService {

    RegisterResponse register(RegisterRequest req);

    void sendPasswordResetCode(/*String email*/);
    //void changePassword(Long userId, ChangePasswordRequest req);
    void changePassword(ChangePasswordRequest req);

    void sendAccountVerificationCode(/*String email*/);
    void verifyAccount(/*String email, *//*String code*/VerifyAccountRequest req);
}
