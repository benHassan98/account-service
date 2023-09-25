package com.odinbook.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.odinbook.model.Account;
import com.odinbook.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class AccountServiceImpl implements AccountService{

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
    public Account createAccount(Account account) throws IOException {

        account.setPassword(passwordEncoder.encode(account.getPassword()));
        Account savedAccount = accountRepository.saveAndFlush(account);

        elasticsearchClient.index(idx->idx
                .index("accounts")
                .id(savedAccount.getId().toString())
                .document(savedAccount)

        );
        return savedAccount;
    }

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public Account findAccountById(Long id) {
        return accountRepository.findById(id).orElse(null);
    }

    @Override
    public Account findAccountByEmail(String email) {
        return accountRepository.findByEmail(email).orElse(null);
    }

    @Override
    public List<Account> findAccountByUserName(String userName) {
        return accountRepository.findByUserName(userName);
    }

    @Override
    public Account updateAccount(Account account) {
        account.setPassword(findAccountById(account.getId()).getPassword());
        return accountRepository.saveAndFlush(account);
    }

    @Override
    public void addFriend(Long addingId, Long addedId) {

        Account addingAccount = findAccountById(addingId);
        Account addedAccount  = findAccountById(addedId);

        addingAccount.addFriend(addedAccount);

        addingAccount.follow(addedAccount);
        addedAccount.follow(addingAccount);


        accountRepository.save(addingAccount);
        accountRepository.save(addedAccount);

    }

    @Override
    public Boolean isEmailUnique(String email) {
        Account account = accountRepository.findByEmail(email).orElse(null);
        return Objects.isNull(account);
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
    public List<Account> searchAccountsByUserNameOrEmail(String searchText) throws IOException {

        return  elasticsearchClient.search(s->s
                        .index("accounts")
                        .query(q->q
                                .multiMatch(v->v
                                        .fields(List.of("userName","email"))
                                        .fuzziness("AUTO")
                                        .query(searchText)
                                )
                        )
                ,
                Account.class
        ).hits().hits().stream().map(Hit::source).toList();

    }

}
