package com.odinbook.service;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.azure.messaging.webpubsub.models.WebPubSubContentType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.model.Account;
import com.odinbook.pojo.Message;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;

@Service
public class WebPubSubServiceImpl implements WebPubSubService{
    @Value("${spring.cloud.azure.pubsub.connection-string}")
    private String connectStr;
    @Override
    public WebPubSubClientAccessToken createClientAndGetToken(GetClientAccessTokenOptions options,
                                                              Function<String, List<Account>> function
                                                              ) throws URISyntaxException {

        WebPubSubServiceClient service = new WebPubSubServiceClientBuilder()
                .connectionString(connectStr)
                .hub("accountSearch")
                .buildClient();

        WebPubSubClientAccessToken token = service.getClientAccessToken(options);

        WebSocketClient webSocketClient = new WebSocketClient(new URI(token.getUrl())) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {

            }

            @Override
            public void onMessage(String jsonString) {
                System.out.println("Message in WebPubSub: "+ jsonString);
                try {
                    Message message = new ObjectMapper().readValue(jsonString,Message.class);
                    service.sendToUser(
                            message.getId().toString(),
                            "It WORKS !!!!!",
                            WebPubSubContentType.TEXT_PLAIN
                    );
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onClose(int i, String s, boolean b) {

            }

            @Override
            public void onError(Exception e) {

            }
        };



        webSocketClient.connect();

                return token;

    }
}
