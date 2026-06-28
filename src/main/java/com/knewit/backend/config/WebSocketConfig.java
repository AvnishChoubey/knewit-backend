package com.knewit.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.app.websocket.endpoint-path}")
    private String endpointPath;

    @Value("${spring.app.websocket.broker-prefix}")
    private String brokerPrefix;

    @Value("${spring.app.websocket.topic-prefix}")
    private String topicPrefix;

    @Value("${spring.app.websocket.queue-prefix}")
    private String queuePrefix;

    @Value("${spring.app.websocket.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // POSTMAN testing
        registry.addEndpoint(endpointPath).setAllowedOrigins("*");

        // production
        registry.addEndpoint(endpointPath).setAllowedOrigins(allowedOrigins.split(","));
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(brokerPrefix);
        registry.enableSimpleBroker(topicPrefix, queuePrefix);
        registry.setUserDestinationPrefix(queuePrefix);
    }
}
