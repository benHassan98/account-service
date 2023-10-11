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
    @Value("${spring.cloud.azure.storage.connection-string}")
    private String blobStorageConnectStr;
    @Value("${spring.cloud.azure.pubsub.connection-string}")
    private String webPubSubConnectStr;

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              PasswordEncoder passwordEncoder,
                              ElasticsearchClient elasticsearchClient
                              ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.elasticsearchClient = elasticsearchClient;

    }

    @Override
    public Account createAccount(Account account){

        account.setPassword(passwordEncoder.encode(account.getPassword()));
        Account savedAccount = accountRepository.saveAndFlush(account);
        try{
            String blobName = Objects.isNull(account.getImage())?
                    Objects.nonNull(account.getPicture())?account.getPicture():
                    "defaultPicture":
                    account.getId()+"/"+account.getImage().getName();

            this.createBlob(blobName,account.getImage());

        }
        catch (IOException exception){
            exception.printStackTrace();
            account.setPicture(
                    Objects.nonNull(account.getPicture())?
                            account.getPicture():
                            "defaultPicture"
            );

        }

        try{
            elasticsearchClient.index(idx->idx
                    .index("accounts")
                    .id(savedAccount.getId().toString())
                    .document(savedAccount)

            );

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
                    try{
                        String blobName = Objects.isNull(newAccount.getImage())?
                                newAccount.getPicture():
                                newAccount.getId()+"/"+newAccount.getImage().getName();

                        createBlob(blobName,newAccount.getImage());
                        newAccount.setPicture(blobName);
                    }
                    catch (IOException exception){
                        exception.printStackTrace();
                    }
                    try{
                        elasticsearchClient.update(u->u
                                        .index("accounts")
                                        .id(newAccount.getId().toString())
                                        .doc(newAccount)
                                , Account.class);
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

    @Override
    public void createBlob(String blobName, MultipartFile image) throws IOException{

        new BlobServiceClientBuilder()
                .connectionString(blobStorageConnectStr)
                .buildClient()
                .getBlobContainerClient("images")
                .getBlobClient(blobName)
                .upload(image.getInputStream());
    }


}
