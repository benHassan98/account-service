package com.odinbook.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.odinbook.model.Account;
import com.odinbook.repository.AccountRepository;
import com.nimbusds.jose.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
public class AccountServiceImpl implements AccountService{

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ElasticsearchClient elasticsearchClient;


    @Value("${spring.cloud.azure.storage.connection-string}")
    private String connectStr;
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

    @Override
    public void addFriend(Long addingId, Long addedId) {

        Account addingAccount,
                addedAccount;

        try{
            addingAccount = findAccountById(addingId)
                    .orElseThrow(NullPointerException::new);
            addedAccount =  findAccountById(addedId)
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

                byte[] blobContent = new BlobServiceClientBuilder()
                        .connectionString(connectStr)
                        .buildClient()
                        .getBlobContainerClient("images")
                        .getBlobClient(account.getPicture())
                        .downloadContent().toBytes();

                account.setPicture(Base64.encode(blobContent).toString());
            }).toList();

        }
        catch (IOException exception){
            exception.printStackTrace();
        }

        return Collections.emptyList();

    }

}
