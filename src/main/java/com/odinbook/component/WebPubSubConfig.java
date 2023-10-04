package com.odinbook.component;


import com.azure.core.http.rest.RequestOptions;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.odinbook.pojo.Message;
import jakarta.annotation.PostConstruct;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;

@Component
@Profile(value = "prod")
public class WebPubSubConfig {

//    @Autowired
//    private AccountService accountService;

    @Value("${spring.cloud.azure.pubsub.connection-string}")
    private String connectStr;

    @PostConstruct
    public void init() throws URISyntaxException {
        System.out.println("Hello from WebSocket");
        WebPubSubServiceClient service = new WebPubSubServiceClientBuilder()
                .connectionString(connectStr)
                .hub("accountSearch")
                .buildClient();


        WebPubSubClientAccessToken token = service.getClientAccessToken(new GetClientAccessTokenOptions());





        WebSocketClient webSocketClient = new WebSocketClient(new URI(token.getUrl())) {
            @Override
            public void onMessage(String message) {
                System.out.printf("Message received: %s%n", message);


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
