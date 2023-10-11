package com.odinbook.accountservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.odinbook.accountservice.model.Account;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ElasticSearchServiceImpl implements ElasticSearchService{

    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public ElasticSearchServiceImpl(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @Override
    public void insertAccount(Account account) throws IOException, ElasticsearchException {
        elasticsearchClient.index(idx->idx
                .index("accounts")
                .id(account.getId().toString())
                .document(account)

        );
    }

    @Override
    public void updateAccount(Account newAccount) throws IOException, ElasticsearchException{
        elasticsearchClient.update(u->u
                        .index("accounts")
                        .id(newAccount.getId().toString())
                        .doc(newAccount)
                , Account.class);
    }

    @Override
    public List<Account> searchAccountsByUserNameOrEmail(String searchText) throws IOException, ElasticsearchException  {
        return  elasticsearchClient.search(s -> s
                        .index("accounts")
                        .query(q -> q
                                .multiMatch(v -> v
                                        .fields(List.of("userName", "email"))
                                        .fuzziness("AUTO")
                                        .query(searchText)
                                )
                        )
                ,
                Account.class
        ).hits().hits().stream().map(Hit::source).toList();
    }
}
