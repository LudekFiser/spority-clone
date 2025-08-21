package com.example.spotifyclone.service.impl;

import com.example.spotifyclone.dto.ChangePasswordRequest;
import com.example.spotifyclone.dto.RegisterRequest;
import com.example.spotifyclone.dto.RegisterResponse;
import com.example.spotifyclone.dto.VerifyAccountRequest;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.enums.ROLE;
import com.example.spotifyclone.exception.NotOldEnoughException;
import com.example.spotifyclone.exception.PasswordsDoNotMatchException;
import com.example.spotifyclone.exception.PasswordsMatchingException;
import com.example.spotifyclone.exception.UserNotFoundException;
import com.example.spotifyclone.mapper.UserMapper;
import com.example.spotifyclone.repository.UserRepository;
import com.example.spotifyclone.service.AuthService;
import com.example.spotifyclone.service.EmailService;
import com.example.spotifyclone.service.UserService;
import com.example.spotifyclone.utils.otp.OtpService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Slf4j
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthService authService;
    private final OtpService otpService;

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

        if(!User.isAdult(req.getDateOfBirth())) {
            throw new NotOldEnoughException();
        }
        var user = userMapper.toEntity(req);

        /*if(user.extractAgeFromBirthDate(req.getDateOfBirth()) < 18) {
            System.out.println(user.extractAgeFromBirthDate(user.getDateOfBirth()));
            throw new NotOldEnoughException();
        }*/
        /*if (user.getAgeFromBirthDate(req.getDateOfBirth())) {
            System.out.println("AGE: "+user.getAgeFromBirthDate(req.getDateOfBirth()));
            throw new NotOldEnoughException();
        }*/


        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(ROLE.USER);
        user.setTwoFactorEmail(false);

        var savedUser = userRepository.save(user);

        try {
            emailService.sendPostRegisterEmail(savedUser.getEmail(), savedUser.getName());
        } catch (MessagingException e) {
            log.warn("Failed to send registration email to {}: {}", savedUser.getEmail(), e.getMessage());
        }


        return userMapper.toResponse(savedUser);
    }

    /*@Override
    public void changePassword(Long userId, ChangePasswordRequest req) {
        var user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if(!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new PasswordsDoNotMatchException();
        }

        if(passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new PasswordsMatchingException();
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }*/

    /*@Override
    public void changePassword(String email, ChangePasswordRequest req) {
        var user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        // Password validations
        if(!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new PasswordsDoNotMatchException();
        }
        if(passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new PasswordsMatchingException();
        }

        // validating user otp
        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }
        if (user.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }


        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiration(null);
        userRepository.save(user);
    }*/
    @Override
    public void changePassword(ChangePasswordRequest req) {
        //var user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        var user = authService.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("User is not authenticated");
        }

        // Password validations
        if(!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            throw new PasswordsDoNotMatchException();
        }
        if(passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new PasswordsMatchingException();
        }

        // validating user otp
        /*if (user.getVerificationCode() == null || !user.getVerificationCode().equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }*/
        if (user.getVerificationCode() == null) {
            throw new RuntimeException("Invalid OTP");
        }
        if (!otpService.verifyOtp(req.getOtp(), user.getVerificationCode())) {
            throw new RuntimeException("Invalid OTP");
        }
        if (user.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }


        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        user.setVerificationCode(null);
        user.setVerificationCodeExpiration(null);
        userRepository.save(user);
    }


    /*@Override
    public void sendPasswordResetCode(String email) {
        var user =  userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        // Generate 6 digit OTP / VERIFICATION CODE
        String otp = otpService.generateOtp();
        // Calculate expiration time (current time + 15 minutes in milliseconds)
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        user.setVerificationCode(otpService.encodeOtp(otp));
        user.setVerificationCodeExpiration(expirationTime);
        userRepository.save(user);

        try {
            emailService.sendResetPasswordCode(user.getEmail(), otp);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to send reset password code to " + user.getEmail(), ex);
        }
    }*/
    @Override
    public void sendPasswordResetCode() {
        var user = authService.getCurrentUser();
        if (user == null) {
            throw new UserNotFoundException();
        }

        // Generate 6 digit OTP / VERIFICATION CODE
        String otp = otpService.generateOtp();
        // Calculate expiration time (current time + 15 minutes in milliseconds)
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        user.setVerificationCode(otpService.encodeOtp(otp));
        user.setVerificationCodeExpiration(expirationTime);
        userRepository.save(user);

        try {
            emailService.sendResetPasswordCode(user.getEmail(), otp);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to send reset password code to " + user.getEmail(), ex);
        }
    }

    @Override
    public void verifyAccount(VerifyAccountRequest req) {
        var user = authService.getCurrentUser();
        if (user == null) {
            throw new UserNotFoundException();
        }

        if (user.isVerified()) {
            throw new RuntimeException("Account is already verified");
        }

        if (user.getVerificationCode() == null) {
            throw new RuntimeException("Invalid OTP");
        }
        if (!otpService.verifyOtp(req.getOtp(), user.getVerificationCode())) {
            throw new RuntimeException("Invalid OTP");
        }
        if (user.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }



        user.setVerificationCode(null);
        user.setVerificationCodeExpiration(null);
        user.setVerified(true);
        userRepository.save(user);
    }

    /*@Override
    public void sendAccountVerificationCode(String email) {
        var user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        if (user.isVerified()) {
            throw new RuntimeException("Account is already verified");
        }

        String otp = otpService.generateOtp();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        user.setVerificationCode(otpService.encodeOtp(otp));
        user.setVerificationCodeExpiration(expirationTime);

        userRepository.save(user);

        try {
            emailService.sendAccountVerificationCode(user.getEmail(), otp);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to send account verification code to " + user.getEmail(), ex);
        }
    }*/

    @Override
    public void sendAccountVerificationCode(/*String email*/) {
        var user = authService.getCurrentUser();
        if (user == null) {
            throw new UserNotFoundException();
        }

        if (user.isVerified()) {
            throw new RuntimeException("Account is already verified");
        }

        String otp = otpService.generateOtp();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        user.setVerificationCode(otpService.encodeOtp(otp));
        user.setVerificationCodeExpiration(expirationTime);

        userRepository.save(user);

        try {
            emailService.sendAccountVerificationCode(user.getEmail(), otp);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to send account verification code to " + user.getEmail(), ex);
        }
    }
}
