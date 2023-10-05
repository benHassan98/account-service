package com.odinbook.accountservice.config;


import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.accountservice.service.AccountService;
import com.odinbook.accountservice.pojo.Message;
import jakarta.annotation.PostConstruct;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
public class WebPubSubConfig {

    @Autowired
    private AccountService accountService;

    @PostConstruct
    public void init() throws URISyntaxException {

        WebPubSubServiceClient service = accountService.getServiceClient();

        WebPubSubClientAccessToken token = service.getClientAccessToken(
                new GetClientAccessTokenOptions()
                        .setUserId("0")
        );

        WebSocketClient webSocketClient = new WebSocketClient(new URI(token.getUrl())) {
            @Override
            public void onMessage(String jsonString) {
                try {
                    Message message = new ObjectMapper().readValue(jsonString,Message.class);
                    service.sendToUser(
                            message.getId().toString(),
                            accountService
                                    .searchAccountsByUserNameOrEmail(message.getContent())
                                    .toString(),
                            WebPubSubContentType.APPLICATION_JSON
                    );
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onClose(int arg0, String arg1, boolean arg2) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onError(Exception arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onOpen(ServerHandshake arg0) {
                // TODO Auto-generated method stub

            }

        };

        webSocketClient.connect();


    }


}
