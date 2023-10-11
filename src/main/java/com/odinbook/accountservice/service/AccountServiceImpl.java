package com.odinbook.accountservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.odinbook.accountservice.repository.AccountRepository;
import com.odinbook.accountservice.model.Account;
import com.odinbook.accountservice.record.AddFriendRecord;
import com.nimbusds.jose.util.Base64;

import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class AccountServiceImpl implements AccountService{
    @Value("${spring.cloud.azure.pubsub.connection-string}")
    private String webPubSubConnectStr;

    private final AccountRepository accountRepository;
    private final ImageService imageService;
    private final ElasticSearchService elasticSearchService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              ImageService imageService,
                              ElasticSearchService elasticSearchService,
                              PasswordEncoder passwordEncoder
                              ) {
        this.accountRepository = accountRepository;
        this.imageService = imageService;
        this.elasticSearchService = elasticSearchService;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public Account createAccount(Account account){

        account.setPassword(passwordEncoder.encode(account.getPassword()));
        Account savedAccount = accountRepository.saveAndFlush(account);

        try{
            elasticSearchService.insertAccount(savedAccount);
        }
        catch (IOException | ElasticsearchException exception){
            exception.printStackTrace();
        }

        return savedAccount;
    }

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public Optional<Account> findAccountById(Long id) {
        return accountRepository.findById(id);
    }

    @Override
    public Optional<Account> findAccountByEmail(String email) {
        return accountRepository.findByEmail(email);
    }

    @Override
    public List<Account> findAccountByUserName(String userName) {
        return accountRepository.findByUserName(userName);
    }

    @Override
    public Optional<Account> updateAccount(Account newAccount) {


        return findAccountById(newAccount.getId())
                .map(oldAccount->{
                    String blobName = Objects.isNull(newAccount.getImage())?
                            newAccount.getPicture():
                            newAccount.getId()+"/"+newAccount.getImage().getName();

                    try{
                        imageService.createBlob(blobName,newAccount.getImage());
                        newAccount.setPicture(blobName);

                        elasticSearchService.updateAccount(newAccount);
                    }
                    catch (IOException | ElasticsearchException exception){
                        exception.printStackTrace();
                    }

                    newAccount.setPassword(oldAccount.getPassword());
                    return accountRepository.saveAndFlush(newAccount);
                });
    }



    @ServiceActivator(inputChannel = "addFriendRequest")
    @Override
    public void addFriend(@Payload AddFriendRecord addFriendRecord) {

        Account addingAccount,
                addedAccount;

        try{
            addingAccount = findAccountById(addFriendRecord.addingId())
                    .orElseThrow();
            addedAccount =  findAccountById(addFriendRecord.addedId())
                    .orElseThrow();
        }
        catch (NoSuchElementException exception){
            exception.printStackTrace();
            return;
        }

        addingAccount.addFriend(addedAccount);

        addingAccount.follow(addedAccount);
        addedAccount.follow(addingAccount);


        accountRepository.save(addingAccount);
        accountRepository.save(addedAccount);

    }

    @Override
    public Boolean isEmailUnique(String email) {
        return findAccountByEmail(email).isEmpty();
    }
    @Override
    public void resetPassword(String newPassword,String email) {
        accountRepository.resetPassword(passwordEncoder.encode(newPassword),email);
    }

    @Override
    public void verifyAccount(String email) {
        accountRepository.verifyAccount(email);
    }

    @Override
    public List<Account> findNewUsers() {
        long threeMonths = 3*365*24*60*60*1000L;
        return  accountRepository.findAll().stream().filter(account->
                new Date().getTime() - account.getCreatedDate().getTime() <= threeMonths
                        && !account.getUserName().equals("ExampleUser")
        ).toList();

    }

    @Override
    public void follow(Long followerId,Long followeeId) {
        accountRepository.follow(followerId,followeeId);
    }

    @Override
    public void unFollow(Long followerId,Long followeeId) {
        accountRepository.unFollow(followerId,followeeId);
    }

    @Override
    public List<Account> searchAccountsByUserNameOrEmail(String searchText){


        try{
            return elasticSearchService.searchAccountsByUserNameOrEmail(searchText);
        }
        catch (IOException | ElasticsearchException exception){
            exception.printStackTrace();
        }

        return Collections.emptyList();

    }

    @Override
    public String getClientAccessToken(Long accountId) {

        GetClientAccessTokenOptions options = new GetClientAccessTokenOptions();
        options.setUserId(accountId.toString());

        return new WebPubSubServiceClientBuilder()
                .connectionString(webPubSubConnectStr)
                .hub("accountSearch")
                .buildClient()
                .getClientAccessToken(options).getUrl();

    }



}
