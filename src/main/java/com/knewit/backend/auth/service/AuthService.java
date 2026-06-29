package com.knewit.backend.auth.service;

import com.knewit.backend.auth.dto.*;
import com.knewit.backend.auth.entity.*;
import com.knewit.backend.auth.enums.AuthProvider;
import com.knewit.backend.auth.enums.Role;
import com.knewit.backend.auth.enums.UserStatus;
import com.knewit.backend.auth.repository.*;
import com.knewit.backend.common.dto.MediaUploadResponse;
import com.knewit.backend.common.enums.Topic;
import com.knewit.backend.common.exception.KnewitException;
import com.knewit.backend.common.service.MediaService;
import com.knewit.backend.search.entity.UserDocument;
import com.knewit.backend.search.service.SearchService;
import com.knewit.backend.user.entity.UserInterest;
import com.knewit.backend.user.repository.UserInterestRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private EmailVerificationTokenRepository verificationTokenRepository;
    @Autowired private UserInterestRepository userInterestRepository;
    @Autowired private MediaService mediaService;
    @Autowired private JwtService jwtService;
    @Autowired private SearchService searchService;
    @Autowired private EmailService emailService;

    @Autowired private PasswordEncoder passwordEncoder;

    @Value("${auth-url}")
    private String authUrl;

    @Transactional
    public EmailSignUpResponse signup(EmailSignUpRequest request) {
        System.out.println("REQUEST CAME IN AUTH SERVICE SIGNUP");
        if(!request.getPassword().equals(request.getConfirmPassword())) {
            throw new KnewitException("DIFFERENT_PASSWORDS", "Passwords do not match", HttpStatus.BAD_REQUEST);
        }

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new KnewitException("EMAIL_ALREADY_EXISTS", "Email is already registered", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .role(Role.USER)
                .isPrivateProfile(false)
                .build();

        userRepository.save(user);

        EmailVerificationToken emailVerificationToken = sendVerificationLink(user);

        System.out.println("VERIFICATION TOKEN ISSUED FOR EMAIL: " + request.getEmail() + " -> " + emailVerificationToken.getToken());

        return EmailSignUpResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .verificationTokenExpiresAt(emailVerificationToken.getExpiresAt().toString())
                .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new KnewitException("INVALID_CREDENTIALS", "Email/password combination is invalid", HttpStatus.BAD_REQUEST));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new KnewitException("INVALID_CREDENTIALS", "Email/password combination is invalid", HttpStatus.BAD_REQUEST);
        }

        if(user.getEmailVerifiedAt() == null) {
            throw new KnewitException("EMAIL_NOT_VERIFIED", "Email account has not been verified", HttpStatus.FORBIDDEN);
        }

        boolean profileIncomplete = (user.getUsername() == null);

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getProfileCompletedAt() != null);

        AuthenticatedUserDto authenticatedUserDto = AuthenticatedUserDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .profileCompleted(!profileIncomplete)
                .verified(true)
                .build();

        return LoginResponse.builder()
                .accessToken(accessToken)
                .user(authenticatedUserDto)
                .profileCompletionRequired(profileIncomplete)
                .build();
    }

    @Transactional
    public VerifyEmailResponse verifyEmail(String token) {
        EmailVerificationToken emailVerificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new KnewitException("INVALID_VERIFICATION_TOKEN", "Token is malformed or not recognized", HttpStatus.BAD_REQUEST));

        if(emailVerificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new KnewitException("VERIFICATION_TOKEN_EXPIRED", "Token has expired", HttpStatus.BAD_REQUEST);
        }

        User user = emailVerificationToken.getUser();
        user.setEmailVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        emailVerificationToken.setVerifiedAt(LocalDateTime.now());
        verificationTokenRepository.save(emailVerificationToken);

        return VerifyEmailResponse.builder()
                .verified(true)
                .profileCompletionRequired(user.getUsername() == null)
                .build();
    }

    @Transactional
    public ResendVerificationResponse resendVerification(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        if (user.getEmailVerifiedAt() != null) {
            throw new KnewitException("EMAIL_ALREADY_VERIFIED", "Email is already verified", HttpStatus.CONFLICT);
        }

        EmailVerificationToken emailVerificationToken = sendVerificationLink(user);

        System.out.println("VERIFICATION TOKEN ISSUED FOR EMAIL: " + request.getEmail() + " -> " + emailVerificationToken.getToken());

        return ResendVerificationResponse.builder()
                .sent(true)
                .verificationTokenExpiresAt(emailVerificationToken.getExpiresAt().toString())
                .build();
    }

    @Transactional
    public ProfileCompletionResponse completeProfile(CustomUserDetails customUserDetails, ProfileCompletionRequest request) {
        if(customUserDetails == null) {
            throw new KnewitException("UNAUTHORIZED_USER", "Unauthorized user", HttpStatus.UNAUTHORIZED);
        }

        Long userId = customUserDetails.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new KnewitException("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND));

        if (user.getProfileCompletedAt() != null) {
            throw new KnewitException("PROFILE_ALREADY_COMPLETED", "Profile is already completed", HttpStatus.CONFLICT);
        }

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new KnewitException("EMPTY_USERNAME", "Username cannot be empty", HttpStatus.BAD_REQUEST);
        }

        if (!request.getUsername().equals(user.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new KnewitException("USERNAME_ALREADY_TAKEN", "Username is already taken", HttpStatus.CONFLICT);
        }

        user.setUsername(request.getUsername());
        user.setBio(request.getBio());

        if(request.getFile() != null) {
            System.out.println("AVATAR UPLOADING START");
            MediaUploadResponse mediaUploadResponse = mediaService.uploadFile(request.getFile(), "/knewit/users/avatars");
            System.out.println("AVATAR UPLOADED SUCCESSFULLY");
            user.setAvatarUrl(mediaUploadResponse.getUrl());
            user.setAvatarPublicId(mediaUploadResponse.getPublicId());
        }

        user.setProfileCompletedAt(LocalDateTime.now());
        userRepository.save(user);

        UserDocument userDoc = UserDocument.builder()
                .id(user.getId().toString())
                .username(user.getUsername())
                .build();
        searchService.enqueueSyncEvent("USER", user.getId().toString(), "UPDATE", userDoc);

        for(String interest : request.getInterests()) {
            UserInterest userInterest = UserInterest.builder()
                    .user(user)
                    .interest(Topic.valueOf(interest))
                    .build();

            userInterestRepository.save(userInterest);
        }

        AuthenticatedUserDto authenticatedUserDto = AuthenticatedUserDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .profileCompleted(true)
                .verified(true)
                .build();

        return new ProfileCompletionResponse(authenticatedUserDto);
    }

    public EmailVerificationToken sendVerificationLink(User user) {
        String token = UUID.randomUUID().toString();

        System.out.println("VERIFICATION TOKEN = " + token);

        EmailVerificationToken emailVerificationToken = EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .verifiedAt(null)
                .build();

        verificationTokenRepository.save(emailVerificationToken);

        String verificationLink = authUrl + "/verify-email?token=" + token;
        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationLink);
            return emailVerificationToken;
        } catch (MessagingException e) {
            throw new RuntimeException("Cannot send email");
        }
    }

    public Map<?,?> logoutUser(String token) {
        if (token != null) {
            jwtService.blacklist(token);
        }

        SecurityContextHolder.clearContext();

        return Map.of("message", "Logged out successfully");
    }
}
