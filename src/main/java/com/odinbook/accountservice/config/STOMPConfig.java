package com.odinbook.accountservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class STOMPConfig implements WebSocketMessageBrokerConfigurer {

  private TaskScheduler messageBrokerTaskScheduler;

  @Autowired
  public void setMessageBrokerTaskScheduler(@Lazy TaskScheduler taskScheduler) {
    this.messageBrokerTaskScheduler = taskScheduler;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config
        .setApplicationDestinationPrefixes("/app")
        .enableSimpleBroker("/exchange")
        .setHeartbeatValue(new long[] { 10000, 20000 })
        .setTaskScheduler(this.messageBrokerTaskScheduler);
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/websocket").setAllowedOriginPatterns("*").withSockJS();
  }

}
