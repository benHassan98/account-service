package com.odinbook.accountservice.config;

import com.odinbook.accountservice.service.AccountServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.Map;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String host;
    @Value("${spring.data.redis.port}")
    private int port;

    @Bean
    RedisConnectionFactory connectionFactory(){
        return new LettuceConnectionFactory(new RedisStandaloneConfiguration(host, port));
    }

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            Map<String, MessageListenerAdapter> listenerAdapterMap) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(listenerAdapterMap.get("findNotifiedAccounts"), new PatternTopic("findNotifiedAccountsChannel"));
        container.addMessageListener(listenerAdapterMap.get("addFriend"), new PatternTopic("addFriendChannel"));

        return container;
    }

    @Bean("findNotifiedAccounts")
    MessageListenerAdapter findNotifiedAccountsListenerAdapter(AccountServiceImpl accountService) {
        return new MessageListenerAdapter(accountService, "findNotifiedAccountsFromPost");
    }

    @Bean("addFriend")
    MessageListenerAdapter addFriendListenerAdapter(AccountServiceImpl accountService) {
        return new MessageListenerAdapter(accountService, "addFriend");
    }

    @Bean
    StringRedisTemplate template(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }


}
