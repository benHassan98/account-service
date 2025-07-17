package com.odinbook.accountservice.config;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import com.odinbook.accountservice.service.AccountServiceImpl;

@Configuration
public class RedisConfig {

  @Bean
  RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
      Map<String, MessageListenerAdapter> listenerAdapterMap) {

    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);

    container.addMessageListener(listenerAdapterMap.get("addFollower"), new PatternTopic("addFollowerChannel"));

    return container;
  }

  @Bean("addFollower")
  MessageListenerAdapter addFollowerListenerAdapter(AccountServiceImpl accountService) {
    return new MessageListenerAdapter(accountService, "addFollower");
  }

  @Bean
  StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
    return new StringRedisTemplate(connectionFactory);
  }

}
