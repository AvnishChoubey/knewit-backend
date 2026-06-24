package com.knewit.backend.auth.dto;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {

        if("google".equals(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        }

        if("facebook".equals(registrationId)) {
            return new FacebookOAuth2UserInfo(attributes);
        }

        throw new RuntimeException("Provider not supported");
    }
}
