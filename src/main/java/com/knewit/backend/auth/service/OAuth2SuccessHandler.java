package com.knewit.backend.auth.service;

import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.repository.UserRepository;
import com.knewit.backend.common.exception.KnewitException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
    @Value("${frontend-url}")
    private String frontendUrl;

    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");

        String providerId = oauthUser.getAttribute("sub");

        User user = userRepository.findByEmail(email).orElseThrow(() -> new KnewitException("asdfghjkl", "asdfghjkl", HttpStatus.BAD_REQUEST));

        String jwt = jwtService.generateToken(user.getId(), user.getEmail(), user.getProfileCompletedAt() != null);

        Cookie cookie = new Cookie("access_token", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        if(user.getProfileCompletedAt() == null) {
            response.sendRedirect(frontendUrl + "/complete-profile");
        } else {
            response.sendRedirect( frontendUrl + "/feed");
        }
    }
}