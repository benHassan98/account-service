package com.odinbook.accountservice.config;

import com.odinbook.accountservice.record.AddFriendRecord;

import com.odinbook.accountservice.record.NotifyAccountsRecord;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Filter;
import org.springframework.integration.amqp.dsl.Amqp;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageSelector;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.json.JsonToObjectTransformer;
import org.springframework.integration.json.ObjectToJsonTransformer;
import org.springframework.integration.router.HeaderValueRouter;
import org.springframework.messaging.MessageChannel;

@Configuration
public class IntegrationConfig {
    @Bean
    public AbstractMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        messageListenerContainer.setQueueNames("odinBook.accountChannel");
        return messageListenerContainer;
    }

    @Bean
    public AmqpInboundChannelAdapter inboundChannelAdapter(AbstractMessageListenerContainer messageListenerContainer) {
        AmqpInboundChannelAdapter adapter = new AmqpInboundChannelAdapter(messageListenerContainer);
        adapter.setOutputChannelName("fromRabbit");
        return adapter;
    }

    @Bean
    public MessageChannel fromRabbit() {
        return new DirectChannel();
    }


    @Bean
    @Filter(
            inputChannel = "fromRabbit",
            outputChannel = "findNotifiedAccountsChannel",
            discardChannel = "addFriendChannel")
    public MessageSelector serviceFilter() {
        return message -> message.getHeaders().containsKey("withNotifiedAccounts");
    }



//    @Bean
//    @ServiceActivator(inputChannel = "fromRabbit")
//    public HeaderValueRouter headerValueRouter() {
//        HeaderValueRouter router = new HeaderValueRouter("service");
//        router.setChannelMapping("addFriendRequest", "addFriendChannel");
//        router.setChannelMapping("findNotifiedAccountsRequest", "findNotifiedAccountsChannel");
//        return router;
//    }

    @Bean
    public MessageChannel findNotifiedAccountsChannel() {
        return new DirectChannel();
    }

    @Bean
    @Transformer(inputChannel = "findNotifiedAccountsChannel", outputChannel = "findNotifiedAccountsRequest")
    public JsonToObjectTransformer findNotifiedAccountsTransformer() {
        return new JsonToObjectTransformer(NotifyAccountsRecord.class);
    }

    @Bean
    public MessageChannel findNotifiedAccountsRequest() {
        return new DirectChannel();
    }


    @Bean
    public MessageChannel addFriendChannel() {
        return new DirectChannel();
    }

    @Bean
    @Transformer(inputChannel = "addFriendChannel", outputChannel = "addFriendRequest")
    public JsonToObjectTransformer addFriendTransformer() {
        return new JsonToObjectTransformer(AddFriendRecord.class);
    }

    @Bean
    public MessageChannel addFriendRequest() {
        return new DirectChannel();
    }


    @Bean
    public MessageChannel toRabbit() {
        return new DirectChannel();
    }

    @Bean
    @Transformer(inputChannel = "toRabbit", outputChannel = "toNotificationChannel")
    public ObjectToJsonTransformer notificationChannelTransformer() {
        return new ObjectToJsonTransformer();
    }

    @Bean
    public MessageChannel toNotificationChannel() {
        return new DirectChannel();
    }
    @ServiceActivator(inputChannel = "toNotificationChannel")
    @Bean
    public AmqpOutboundEndpoint amqpOutboundEndpoint(AmqpTemplate amqpTemplate) {
        AmqpOutboundEndpoint adapter = new AmqpOutboundEndpoint(amqpTemplate);
        adapter.setRoutingKey("odinBook.notificationChannel");
        return adapter;
    }





}
