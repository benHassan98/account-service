package com.odinbook.accountservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.accountservice.record.NotifyAccountsRecord;
import com.odinbook.accountservice.repository.AccountRepository;
import com.odinbook.accountservice.model.Account;
import com.odinbook.accountservice.record.AddFriendRecord;
import com.nimbusds.jose.util.Base64;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.util.*;

@Service
public class AccountServiceImpl implements AccountService{

    @PersistenceContext
    private EntityManager entityManager;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository,
                              PasswordEncoder passwordEncoder,
                              StringRedisTemplate stringRedisTemplate
                              ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.stringRedisTemplate = stringRedisTemplate;

    }

    @Override
    public Account createAccount(Account account){

        account.setPassword(passwordEncoder.encode(account.getPassword()));

        return accountRepository.saveAndFlush(account);
    }

    @Override
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Override
    public Account findAccountById(Long id) throws NoSuchElementException {
        return accountRepository.findById(id)
                .orElseThrow();
    }

    @Override
    public Account findAccountByEmail(String email)  throws NoSuchElementException {
        return accountRepository.findByEmail(email)
                .orElseThrow();
    }

    @Override
    public List<Account> findAccountByUserName(String userName) {
        return accountRepository.findByUserName(userName);
    }

    @Override
    public Account updateAccount(Account newAccount) throws NoSuchElementException {


        return accountRepository.findById(newAccount.getId())
                .map(oldAccount->{
                    newAccount.setPassword(oldAccount.getPassword());
                    return accountRepository.saveAndFlush(newAccount);
                })
                .orElseThrow();
    }




    @Override
    @Transactional
    public void addFriend(String addFriendRecordJson) {

        AddFriendRecord addFriendRecord;

        try{
            addFriendRecord = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(addFriendRecordJson, AddFriendRecord.class);
        }
        catch (JsonProcessingException exception){
            exception.printStackTrace();
            return;
        }

        Account addingAccount,
                addedAccount;

        try{
            addingAccount = accountRepository.findById(addFriendRecord.addingId())
                    .orElseThrow();
            addedAccount =  accountRepository.findById(addFriendRecord.addedId())
                    .orElseThrow();
        }
        catch (NoSuchElementException exception){
            exception.printStackTrace();
            return;
        }

        addingAccount.addFriend(addedAccount);
        addedAccount.addFriend(addingAccount);

        addingAccount.follow(addedAccount);
        addedAccount.follow(addingAccount);


        accountRepository.save(addingAccount);
        accountRepository.save(addedAccount);

    }
    @Override
    public void removeFriend(Long removingId, Long removedId){

        Account removingAccount,
                removedAccount;

        try{
            removingAccount = accountRepository.findById(removingId)
                    .orElseThrow();
            removedAccount =  accountRepository.findById(removedId)
                    .orElseThrow();
        }
        catch (NoSuchElementException exception){
            exception.printStackTrace();
            return;
        }

        removingAccount.removeFriend(removedAccount);
        removedAccount.removeFriend(removingAccount);

        accountRepository.saveAndFlush(removingAccount);
        accountRepository.saveAndFlush(removedAccount);

    }

    @Override
    @Transactional
    public List<Long> findNotifiedAccountsToNewPost(NotifyAccountsRecord notifyAccountsRecord){

        Account account;

        try{
            account = accountRepository.findById(notifyAccountsRecord.accountId())
                    .orElseThrow();
        }
        catch (NoSuchElementException exception){
            exception.printStackTrace();
            return Collections.emptyList();
        }

        List<Long> accountList = new ArrayList<>(account.getFriendList()
                .stream().map(Account::getId).filter(accountId ->
                        (
                                notifyAccountsRecord.visibleToFriendList().contains(accountId)
                                && notifyAccountsRecord.friendsVisibilityType()
                        ) ||
                                (
                                        !notifyAccountsRecord.visibleToFriendList().contains(accountId)
                                                && !notifyAccountsRecord.friendsVisibilityType()
                                )
                ).toList());

        if(notifyAccountsRecord.isVisibleToFollowers()){
            accountList.addAll(
                    account.getFollowerList().stream().map(Account::getId).toList()
            );
        }

        accountList.add(account.getId());

        return accountList;
    }



    @Override
    @Transactional
    public void findNotifiedAccountsFromPost(String notifyAccountsJson) {

        NotifyAccountsRecord notifyAccountsRecord;

        try{
            notifyAccountsRecord = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(notifyAccountsJson, NotifyAccountsRecord.class);
        }
        catch (JsonProcessingException exception){
            exception.printStackTrace();
            return;
        }



        List<Long> notifiedList = findNotifiedAccountsToNewPost(notifyAccountsRecord);

        NotifyAccountsRecord notifiedAccountsRecord = new NotifyAccountsRecord(
                notifyAccountsRecord.id(),
                notifyAccountsRecord.accountId(),
                notifyAccountsRecord.postId(),
                notifyAccountsRecord.isShared(),
                notifyAccountsRecord.isVisibleToFollowers(),
                notifyAccountsRecord.friendsVisibilityType(),
                notifyAccountsRecord.visibleToFriendList(),
                notifiedList
        );

        String notifiedAccountsRecordJson;

        try{
            notifiedAccountsRecordJson = new ObjectMapper().writeValueAsString(notifiedAccountsRecord);
        }
        catch (JsonProcessingException exception){
            exception.printStackTrace();
            return;
        }

        stringRedisTemplate.convertAndSend("newPostChannel", notifiedAccountsRecordJson);


    }

    @Override
    public Boolean isEmailUnique(String email) {
        return accountRepository.findByEmail(email).isEmpty();
    }
    @Override
    @Transactional
    public void resetPassword(String newPassword,String email) {
        String savedNewPassword = passwordEncoder.encode(newPassword);

        entityManager
                .createNativeQuery("UPDATE accounts SET password = :newPassword WHERE email = :email")
                .setParameter("newPassword",savedNewPassword)
                .setParameter("email",email)
                .executeUpdate();

    }

    @Override
    @Transactional
    public void verifyAccount(String email) {

        entityManager
                .createNativeQuery("UPDATE accounts SET is_verified = true WHERE email = :email")
                .setParameter("email",email)
                .executeUpdate();

    }

    @Override
    public List<Long> findNewUsers() {
        long threeMonths = 3*365*24*60*60*1000L;

        return accountRepository.findAll().stream().filter(account->
                new Date().toInstant().getEpochSecond() - account.getCreatedDate().getTime() <= threeMonths
        ).map(Account::getId).toList();

    }

    @Override
    @Transactional
    public void follow(Long followerId,Long followeeId) {

        entityManager
                .createNativeQuery("INSERT INTO followers VALUES(:followerId,:followeeId)")
                .setParameter("followerId",followerId)
                .setParameter("followeeId",followeeId)
                .executeUpdate();

    }

    @Override
    @Transactional
    public void unFollow(Long followerId,Long followeeId) {
        entityManager
                .createNativeQuery("DELETE FROM followers WHERE follower_id = :followerId AND followee_id = :followeeId")
                .setParameter("followerId",followerId)
                .setParameter("followeeId",followeeId)
                .executeUpdate();
    }

    @Override
    public List<Account> searchAccountsByUserNameOrEmail(String searchText){

        return accountRepository.searchAccountsByUserNameOrEmail("%"+searchText+"%");

    }





}
