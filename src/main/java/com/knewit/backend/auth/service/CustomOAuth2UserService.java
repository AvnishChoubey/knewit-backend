package com.knewit.backend.auth.service;

import com.knewit.backend.auth.dto.OAuth2UserInfo;
import com.knewit.backend.auth.dto.OAuth2UserInfoFactory;
import com.knewit.backend.auth.entity.User;
import com.knewit.backend.auth.enums.AuthProvider;
import com.knewit.backend.auth.enums.Role;
import com.knewit.backend.auth.enums.UserStatus;
import com.knewit.backend.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    @Autowired private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oauthUser = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauthUser.getAttributes());

        User user = userRepository.findByEmail(userInfo.getEmail()).orElseGet(() -> {
            User newUser = User.builder()
                    .email(userInfo.getEmail())
                    .username(userInfo.getName())
                    .status(UserStatus.ACTIVE)
                    .emailVerifiedAt(LocalDateTime.now())
                    .provider(AuthProvider.valueOf(registrationId.toUpperCase()))
//                        .providerId(userInfo.getId())
                    .role(Role.USER)
                    .build();

            return userRepository.save(newUser);
        });

        return oauthUser;
    }
}