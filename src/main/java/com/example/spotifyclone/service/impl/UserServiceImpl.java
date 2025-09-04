package com.example.spotifyclone.service.impl;

import com.example.spotifyclone.dto.auth.*;
import com.example.spotifyclone.dto.image.UploadedImageDto;
import com.example.spotifyclone.entity.Image;
import com.example.spotifyclone.entity.User;
import com.example.spotifyclone.enums.ROLE;
import com.example.spotifyclone.exception.NotOldEnoughException;
import com.example.spotifyclone.exception.PasswordsDoNotMatchException;
import com.example.spotifyclone.exception.PasswordsMatchingException;
import com.example.spotifyclone.exception.UserNotFoundException;
import com.example.spotifyclone.mapper.UserMapper;
import com.example.spotifyclone.repository.IdeaRepository;
import com.example.spotifyclone.repository.ImageRepository;
import com.example.spotifyclone.repository.UserRepository;
import com.example.spotifyclone.service.AuthService;
import com.example.spotifyclone.service.EmailService;
import com.example.spotifyclone.service.UserService;
import com.example.spotifyclone.utils.cloudinary.CloudinaryService;
import com.example.spotifyclone.utils.otp.OtpService;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final CloudinaryService cloudinaryService;
    private final ImageRepository imageRepository;


    @Override
    public RegisterResponse register(RegisterRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException();
        }

        if(!User.isAdult(req.getDateOfBirth())) {
            throw new NotOldEnoughException();
        }

        User user = userMapper.toEntity(req);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(ROLE.USER);
        user.setTwoFactorEmail(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setAvatarId(null);

        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public RegisterResponse updateUser(UpdateUserRequest req) {
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User is not authenticated");
        }

        if (req.getName() != null) {
            currentUser.setName(req.getName());
        }
        if (req.getEmail() != null) {
            currentUser.setEmail(req.getEmail());
        }
        if (req.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(req.getPhoneNumber());
        }
        if (req.getTwoFactorEmail() != null) {
            currentUser.setTwoFactorEmail(req.getTwoFactorEmail());
        }

        userRepository.save(currentUser);
        return userMapper.toResponse(currentUser);
    }

    @Override
    public CurrentUserDto changeProfilePicture(MultipartFile image) {
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User is not authenticated");
        }

        String uploadedPublicId = null;
        try {
            if (image == null || image.isEmpty()) {
                throw new RuntimeException("No image provided");
            }
            if(currentUser.getAvatarId() != null){
                Image oldImage = currentUser.getAvatarId();
                cloudinaryService.deleteImageByPublicId(oldImage.getPublicId());
                imageRepository.delete(oldImage);
                currentUser.setAvatarId(null);
            }

            // Uplaod new
            UploadedImageDto dto = cloudinaryService.uploadImage(image, "avatars");
            uploadedPublicId = dto.getPublicId();

            Image newImage = cloudinaryService.buildProfileImage(dto, currentUser);
            imageRepository.save(newImage);

            currentUser.setAvatarId(newImage);
            userRepository.save(currentUser);

            return userMapper.toUserDto(currentUser);
        } catch (RuntimeException ex) {
            // DB fail -> zkus smazat hajzla v Cloudinary (best effort)
            if (uploadedPublicId != null) {
                try { cloudinaryService.deleteImageByPublicId(uploadedPublicId); }
                catch (Exception cleanupEx) { log.warn("Cleanup of Cloudinary image failed: {}", cleanupEx.getMessage()); }
            }
            throw ex;
        }
    }

    @Override
    public void deleteUser(DeleteAccountDto otp) {
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User is not authenticated");
        }

        if (currentUser.getVerificationCode() == null) {
            throw new RuntimeException("Invalid OTP");
        }
        if (!otpService.verifyOtp(otp.getOtp(), currentUser.getVerificationCode())) {
            throw new RuntimeException("Invalid gaga OTP");
        }
        if (currentUser.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (currentUser.getAvatarId() != null) {
            Image image = currentUser.getAvatarId();

            cloudinaryService.deleteImageByPublicId(image.getPublicId());

            currentUser.setAvatarId(null);
            userRepository.save(currentUser);
            imageRepository.delete(image);
        }

        userRepository.delete(currentUser);
    }

    @Override
    public void sendAccountDeletionCode() {
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new UserNotFoundException();
        }

        // Generate 6 digit OTP / VERIFICATION CODE
        String otp = otpService.generateOtp();
        System.out.println("GENERATED DELETE OTP: "+otp);
        // Calculate expiration time (current time + 15 minutes in milliseconds)
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        currentUser.setVerificationCode(otpService.encodeOtp(otp));
        currentUser.setVerificationCodeExpiration(expirationTime);
        userRepository.save(currentUser);

        try {
            emailService.sendAccountDeletionCode(currentUser.getEmail(), otp);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to send account deletion code to " + currentUser.getEmail(), ex);
        }
    }

    @Override
    public void deleteProfilePicture() {
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User is not authenticated");
        }

        var usersAvatar = currentUser.getAvatarId();
        if (usersAvatar == null) {
            throw new RuntimeException("You have no avatar");
        }


        var image = imageRepository.findById(usersAvatar.getId()).orElseThrow();
        if (image.getId().equals(usersAvatar.getId())) {
            cloudinaryService.deleteImageByPublicId(image.getPublicId());
            imageRepository.delete(image);
            currentUser.setAvatarId(null);
            userRepository.save(currentUser);
        }
    }


    @Override
    public void changePassword(ChangePasswordRequest req) {
        //var user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("User is not authenticated");
        }

        // Password validations
        if(!passwordEncoder.matches(req.getOldPassword(), currentUser.getPassword())) {
            throw new PasswordsDoNotMatchException();
        }
        if(passwordEncoder.matches(req.getNewPassword(), currentUser.getPassword())) {
            throw new PasswordsMatchingException();
        }

        // validating user otp

        if (currentUser.getVerificationCode() == null) {
            throw new RuntimeException("Invalid OTP");
        }
        if (!otpService.verifyOtp(req.getOtp(), currentUser.getVerificationCode())) {
            throw new RuntimeException("Invalid OTP");
        }
        if (currentUser.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }


        currentUser.setPassword(passwordEncoder.encode(req.getNewPassword()));
        currentUser.setVerificationCode(null);
        currentUser.setVerificationCodeExpiration(null);
        userRepository.save(currentUser);
    }

    @Override
    public void sendPasswordResetCode() {
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new UserNotFoundException();
        }

        // Generate 6 digit OTP / VERIFICATION CODE
        String otp = otpService.generateOtp();
        // Calculate expiration time (current time + 15 minutes in milliseconds)
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        currentUser.setVerificationCode(otpService.encodeOtp(otp));
        currentUser.setVerificationCodeExpiration(expirationTime);
        userRepository.save(currentUser);

        try {
            emailService.sendResetPasswordCode(currentUser.getEmail(), otp);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to send reset password code to " + currentUser.getEmail(), ex);
        }
    }

    @Override
    public void verifyAccount(VerifyAccountRequest req) {
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new UserNotFoundException();
        }

        if (currentUser.isVerified()) {
            throw new RuntimeException("Account is already verified");
        }

        if (currentUser.getVerificationCode() == null) {
            throw new RuntimeException("Invalid OTP");
        }
        if (!otpService.verifyOtp(req.getOtp(), currentUser.getVerificationCode())) {
            throw new RuntimeException("Invalid OTP");
        }
        if (currentUser.getVerificationCodeExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP expired");
        }



        currentUser.setVerificationCode(null);
        currentUser.setVerificationCodeExpiration(null);
        currentUser.setVerified(true);
        userRepository.save(currentUser);
    }

    @Override
    public void sendAccountVerificationCode() {
        var currentUser = authService.getCurrentUser();
        if (currentUser == null) {
            throw new UserNotFoundException();
        }

        if (currentUser.isVerified()) {
            throw new RuntimeException("Account is already verified");
        }

        String otp = otpService.generateOtp();
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);

        currentUser.setVerificationCode(otpService.encodeOtp(otp));
        currentUser.setVerificationCodeExpiration(expirationTime);

        userRepository.save(currentUser);

        try {
            emailService.sendAccountVerificationCode(currentUser.getEmail(), otp);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to send account verification code to " + currentUser.getEmail(), ex);
        }
    }
}
