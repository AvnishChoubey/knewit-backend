package com.knewit.backend.config;

import com.knewit.backend.auth.service.JwtAuthFilter;
import com.knewit.backend.auth.service.OAuth2SuccessHandler;
import com.knewit.backend.auth.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired private JwtAuthFilter jwtAuthFilter;
    @Autowired private CustomOAuth2UserService customOAuth2UserService;
    @Autowired private OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // AUTH
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        // USERS
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/me").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/user", "/api/v1/user/").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/*/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/user/*/**").hasRole("USER")
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/user/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/user/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/user/**").hasRole("USER")
                        // COMMENTS
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/*/comments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts/*/comments/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/posts/*/comments/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/posts/*/comments/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/*/comments/**").hasRole("USER")
                        // POSTS
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/posts/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/posts/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/**").hasRole("USER")
                        // SUBREDDITS
                        .requestMatchers(HttpMethod.GET, "/api/v1/subreddits/*/posts").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/subreddits/*/**").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/subreddits").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/subreddits/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/subreddits/**").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/subreddits/**").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/subreddits/**").hasRole("USER")
                        // SEARCH
                        .requestMatchers(HttpMethod.GET, "/api/v1/search").permitAll()
                        // CHAT
                        .requestMatchers("/api/v1/chat").hasRole("USER")
                        .requestMatchers("/api/v1/chat/*/**").hasRole("USER")
                        .requestMatchers("/api/v1/chat/**").hasRole("USER")
                        .anyRequest().authenticated())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)).successHandler(oAuth2SuccessHandler))
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.getWriter().write("""
                                    {"message":"Logged out successfully"}
                            """);
                        })
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new org.springframework.security.web.authentication.HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}