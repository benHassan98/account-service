package com.odinbook.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.azure.messaging.webpubsub.WebPubSubServiceClientBuilder;
import com.azure.messaging.webpubsub.models.GetClientAccessTokenOptions;
import com.azure.messaging.webpubsub.models.WebPubSubClientAccessToken;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.odinbook.model.Account;
import com.odinbook.record.AddFriendRecord;
import com.odinbook.repository.AccountRepository;
import com.nimbusds.jose.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Service
public class AccountServiceImpl implements AccountService{
    @Value("${spring.cloud.azure.pubsub.connection-string}")
    private String webPubSubConnectStr;

    private final AccountRepository accountRepository;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;
    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              ImageService imageService,
                              PasswordEncoder passwordEncoder,
                              ElasticsearchClient elasticsearchClient
                              ) {
        this.accountRepository = accountRepository;
        this.imageService = imageService;
        this.passwordEncoder = passwordEncoder;
        this.elasticsearchClient = elasticsearchClient;

    }

    @Override
    public Account createAccount(Account account){

        account.setPassword(passwordEncoder.encode(account.getPassword()));
        Account savedAccount = accountRepository.saveAndFlush(account);

        try{
            elasticsearchClient.index(idx->idx
                    .index("accounts")
                    .id(savedAccount.getId().toString())
                    .document(savedAccount)

            );
        }
        catch (IOException exception){
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
    public Optional<Account> updateAccount(Account account) {


        return findAccountById(account.getId())
                .map(savedAccount->{
                    account.setPassword(savedAccount.getPassword());
                    return accountRepository.saveAndFlush(account);
                });
    }



    @ServiceActivator(inputChannel = "addFriendRequest")
    @Override
    public void addFriend(@Payload AddFriendRecord addFriendRecord) {

        Account addingAccount,
                addedAccount;

        try{
            addingAccount = findAccountById(addFriendRecord.addingId())
                    .orElseThrow(NullPointerException::new);
            addedAccount =  findAccountById(addFriendRecord.addedId())
                    .orElseThrow(NullPointerException::new);
        }
        catch (NullPointerException exception){
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
            ).hits().hits().stream().map(Hit::source).filter(Objects::nonNull).peek(account -> {

                account.setPicture(
                                Base64.encode(imageService.findBlob(account.getPicture())).toString()
                        );
            }).toList();

        }
        catch (IOException exception){
            exception.printStackTrace();
        }

        return Collections.emptyList();

    }

    @Override
    public WebPubSubServiceClient getServiceClient() {
        return new WebPubSubServiceClientBuilder()
                .connectionString(webPubSubConnectStr)
                .hub("accountSearch")
                .buildClient();
    }

    @Override
    public String getClientAccessToken(Long accountId) {

        GetClientAccessTokenOptions options = new GetClientAccessTokenOptions();
        options.setUserId(accountId.toString());

        return this
                .getServiceClient()
                .getClientAccessToken(options).getUrl();

    }


}
