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

    @Value("${app.websocket.endpoint-path:/ws}")
    private String endpointPath;

    @Value("${app.websocket.broker-prefix:/app}")
    private String brokerPrefix;

    @Value("${app.websocket.topic-prefix:/topic}")
    private String topicPrefix;

    @Value("${app.websocket.queue-prefix:/queue}")
    private String queuePrefix;

    @Value("${app.websocket.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(endpointPath)
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes(brokerPrefix);
        registry.enableSimpleBroker(topicPrefix, queuePrefix);
        registry.setUserDestinationPrefix(queuePrefix);
    }
}
