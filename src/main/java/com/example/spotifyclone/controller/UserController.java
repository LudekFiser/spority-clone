package com.example.spotifyclone.controller;

import com.example.spotifyclone.dto.ChangePasswordRequest;
import com.example.spotifyclone.dto.RegisterRequest;
import com.example.spotifyclone.dto.RegisterResponse;
import com.example.spotifyclone.dto.VerifyAccountRequest;
import com.example.spotifyclone.service.UserService;
import com.example.spotifyclone.utils.rateLimit.RateLimit;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "users")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            UriComponentsBuilder uriBuilder) {
        var user =  userService.register(registerRequest);

        var uri = uriBuilder
                .path("/api/users/{id}")
                .buildAndExpand(user.getId())
                .toUri();
        return ResponseEntity.created(uri).body(user);
    }

    /*@PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send-password-reset-code")
    public ResponseEntity<Void> sendPasswordResetCode(@RequestParam String email) {
        userService.sendPasswordResetCode(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send-account-verification-code")
    public ResponseEntity<Void> sendAccountVerificationCode(@RequestParam String email) {
        userService.sendAccountVerificationCode(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-account")
    public ResponseEntity<Void> verifyAccount(@Valid @RequestBody String otp, String email) {
        userService.verifyAccount(email ,otp);
        return ResponseEntity.noContent().build();
    }*/

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send-password-reset-code")  // might also be forgot-password endpoint
    public ResponseEntity<Void> sendPasswordResetCode() {
        userService.sendPasswordResetCode();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send-account-verification-code")
    @RateLimit(requests = 3, timeAmount = 1, timeUnit = TimeUnit.HOURS, keyPrefix = "send-verification-code")
    public ResponseEntity<Void> sendAccountVerificationCode() {
        userService.sendAccountVerificationCode();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-account")
    public ResponseEntity<Void> verifyAccount(@Valid @RequestBody VerifyAccountRequest request) {
        userService.verifyAccount(request);
        return ResponseEntity.noContent().build();
    }
}
