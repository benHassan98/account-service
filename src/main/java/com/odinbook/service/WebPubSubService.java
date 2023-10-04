package com.odinbook.service;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.odinbook.model.Account;

import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Function;

public interface WebPubSubService {
    public WebPubSubClientAccessToken createClientAndGetToken(GetClientAccessTokenOptions options,
                                                              Function<String, List<Account>> function) throws URISyntaxException;
}
