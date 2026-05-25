package com.example.attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Topic endpoint for server-to-client broadcasts
        config.enableSimpleBroker("/topic");
        // Application endpoint for client-to-server messages (if any)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the STOMP websocket endpoint that clients connect to
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Enable cross-origin connections
                .withSockJS(); // Fallback for browsers that don't support native websockets
    }
}
