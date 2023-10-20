package com.odinbook.accountservice.config;


import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.accountservice.record.MessageRecord;
import com.odinbook.accountservice.service.AccountService;
import jakarta.annotation.PostConstruct;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class WebPubSubConfig {

//    @Value("${spring.cloud.azure.pubsub.connection-string}")
//    private String webPubSubConnectStr;
//
//    @Autowired
//    private AccountService accountService;
//
//    @PostConstruct
//    public void init() throws URISyntaxException {
//
//        WebPubSubServiceClient service = new WebPubSubServiceClientBuilder()
//                .connectionString(webPubSubConnectStr)
//                .hub("accountSearch")
//                .buildClient();
//
//        WebPubSubClientAccessToken token = service.getClientAccessToken(
//                new GetClientAccessTokenOptions()
//                        .setUserId("0")
//        );
//
//        WebSocketClient webSocketClient = new WebSocketClient(new URI(token.getUrl())) {
//            @Override
//            public void onMessage(String jsonString) {
//                try {
//                    MessageRecord message = new ObjectMapper().readValue(jsonString,MessageRecord.class);
//                    service.sendToUser(
//                            message.id().toString(),
//                            accountService
//                                    .searchAccountsByUserNameOrEmail(message.content())
//                                    .toString(),
//                            WebPubSubContentType.APPLICATION_JSON
//                    );
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onClose(int arg0, String arg1, boolean arg2) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onError(Exception arg0) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onOpen(ServerHandshake arg0) {
//                // TODO Auto-generated method stub
//
//            }
//
//        };
//
//        webSocketClient.connect();
//
//
//    }


}
