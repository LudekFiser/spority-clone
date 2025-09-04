package com.example.spotifyclone.controller;

import com.example.spotifyclone.dto.auth.CurrentUserDto;
import com.example.spotifyclone.dto.auth.LoginRequest;
import com.example.spotifyclone.dto.auth.TwoFARequest;
import com.example.spotifyclone.dto.auth.TwoFAResponse;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.jwt.JwtConfig;
import com.example.spotifyclone.jwt.JwtResponse;
import com.example.spotifyclone.jwt.JwtService;
import com.example.spotifyclone.mapper.UserMapper;
import com.example.spotifyclone.repository.UserRepository;
import com.example.spotifyclone.service.EmailService;
import com.example.spotifyclone.service.UserService;
import com.example.spotifyclone.utils.otp.OtpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
@Tag(name = "auth")
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserRepository userRepository;
    private final UserMapper  userMapper;
    private final OtpService otpService;
    private final UserService userService;
    private final EmailService emailService;


    /*@PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);


        var cookie = new Cookie("refreshToken", refreshToken.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());  // 7 days
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponse(accessToken.toString()));
    }*/

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response)
    {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword())
        );
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        if(user.getTwoFactorEmail()) {
            try {
                String code = otpService.generateOtp();
                LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);
                user.setVerificationCode(otpService.encodeOtp(code));
                user.setVerificationCodeExpiration(expirationTime);
                userRepository.save(user);

                emailService.send2FAVerificationCode(user.getEmail(), code);

                return ResponseEntity.ok(new TwoFAResponse(
                        "2FA Required",
                        user.getId()
                ));
            } catch (Exception e) {
                log.error("Failed to send 2FA code to user: {}", user.getEmail(), e);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiration(null);
                userRepository.save(user);

                throw new RuntimeException("Failed to send 2FA verification code. Please try again.");
            }
        }

        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);


        var cookie = new Cookie("refreshToken", refreshToken.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());  // 7 days
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponse(accessToken.toString()));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<JwtResponse> verify2fa(
            @Valid @RequestBody TwoFARequest req,
            HttpServletResponse response
    ) {
        var user = userRepository.findById(req.getUserId()).orElseThrow();

        if (user.getVerificationCode() == null) {
            throw new RuntimeException("Invalid OTP");
        }
        if (user.getVerificationCodeExpiration() == null ||
            user.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }
        if (!otpService.verifyOtp(req.getOtp(), user.getVerificationCode())) {
            throw new RuntimeException("Invalid OTP");
        }

        // Reset 2FA State
        user.setVerificationCode(null);
        user.setVerificationCodeExpiration(null);
        userRepository.save(user);


        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);


        var cookie = new Cookie("refreshToken", refreshToken.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());  // 7 days
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponse(accessToken.toString()));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserDto> me() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId = (Long) authentication.getPrincipal();

        var user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        var userDto = userMapper.toUserDto(user);

        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue(value = "refreshToken") String refreshToken
    ) {
        var jwt = jwtService.parseToken(refreshToken);
        if(jwt == null || jwt.isExpired()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var user = userRepository.findById(jwt.getUserId()).orElseThrow();
        var accessToken = jwtService.generateAccessToken(user);

        return ResponseEntity.ok(new JwtResponse(accessToken.toString()));
    }


    // helper method might use?

    private ResponseEntity<JwtResponse> getResponseEntity(HttpServletResponse response, User user) {
        var accessToken = jwtService.generateAccessToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);


        var cookie = new Cookie("refreshToken", refreshToken.toString());
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(jwtConfig.getRefreshTokenExpiration());  // 7 days
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponse(accessToken.toString()));
    }
}
