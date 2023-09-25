package com.odinbook.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchClientConfig {

    @Bean
    public ElasticsearchClient elasticsearchClient(
            @Value("${spring.elasticsearch.uri}") String uri,
            @Value("${spring.elasticsearch.username}") String userName,
            @Value("${spring.elasticsearch.password}") String password

    ){
        final CredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(
                        userName,
                        password));

        RestClient restClient = RestClient
                .builder(HttpHost.create(uri))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider))
                .build();

        ElasticsearchTransport elasticsearchTransport = new RestClientTransport(restClient,new JacksonJsonpMapper());

        return new ElasticsearchClient(elasticsearchTransport);

    }

}
