package com.odinbook.accountservice.service;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import com.odinbook.accountservice.model.Account;

import java.io.IOException;
import java.util.List;

public interface ElasticSearchService {
    public void insertAccount(Account account) throws IOException, ElasticsearchException;
    public void updateAccount(Account newAccount) throws IOException, ElasticsearchException;
    public List<Account> searchAccountsByUserNameOrEmail(String searchText) throws IOException, ElasticsearchException;
}
