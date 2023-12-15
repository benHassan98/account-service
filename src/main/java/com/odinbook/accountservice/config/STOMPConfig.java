package com.odinbook.accountservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class STOMPConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host}")
    private String host;
    @Value("${broker.port}")
    private Integer port;
    @Value("${spring.rabbitmq.username}")
    private String userName;
    @Value("${spring.rabbitmq.password}")
    private String password;
    @Value("${gatewayService.url}")
    private String gatewayUrl;
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config
                .enableStompBrokerRelay("/exchange")
                .setRelayHost(host)
                .setRelayPort(port)
                .setClientLogin(userName)
                .setClientPasscode(password);


    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/account/websocket").setAllowedOrigins(gatewayUrl).withSockJS();
    }

}
