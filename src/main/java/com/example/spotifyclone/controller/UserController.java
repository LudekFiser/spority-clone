package com.example.spotifyclone.controller;

import com.example.spotifyclone.dto.ChangePasswordRequest;
import com.example.spotifyclone.dto.RegisterRequest;
import com.example.spotifyclone.dto.RegisterResponse;
import com.example.spotifyclone.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
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

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changePassword(changePasswordRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/send-password-reset-code")
    public ResponseEntity<Void> sendPasswordResetCode(@RequestParam String email) {
        userService.sendPasswordResetCode(email);
        return ResponseEntity.noContent().build();
    }
}
