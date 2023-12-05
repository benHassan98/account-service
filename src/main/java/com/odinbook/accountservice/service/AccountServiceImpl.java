package com.odinbook.accountservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
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
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class AccountServiceImpl implements AccountService{

    @PersistenceContext
    private EntityManager entityManager;
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
                    String blobName = Objects.isNull(newAccount.getImage())?
                            newAccount.getPicture():
                            newAccount.getImage().getId();
                    try{
                        imageService.createBlob(blobName,newAccount.getImage().getFile());
                        newAccount.setPicture(blobName);
                        newAccount.setImage(null);
                    }
                    catch (IOException exception){
                        exception.printStackTrace();
                    }

                    try{
                        elasticSearchService.updateAccount(newAccount);
                    }
                    catch (IOException | ElasticsearchException exception){
                        exception.printStackTrace();
                    }

                    newAccount.setPassword(oldAccount.getPassword());
                    return accountRepository.saveAndFlush(newAccount);
                })
                .orElseThrow();
    }




    @Override
    @ServiceActivator(inputChannel = "addFriendRequest")
    @Transactional
    public void addFriend(@Payload AddFriendRecord addFriendRecord) {

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
    @ServiceActivator(inputChannel = "findNotifiedAccountsRequest", outputChannel = "toRabbit")
    @Transactional
    public NotifyAccountsRecord findNotifiedAccountsFromPost(Message<NotifyAccountsRecord> message) {
        System.out.println(message);
        NotifyAccountsRecord notifyAccountsMessage = message.getPayload();

        List<Long>notifiedList = findNotifiedAccountsToNewPost(notifyAccountsMessage);

        return new NotifyAccountsRecord(
                notifyAccountsMessage.id(),
                notifyAccountsMessage.accountId(),
                notifyAccountsMessage.postId(),
                notifyAccountsMessage.isShared(),
                notifyAccountsMessage.isVisibleToFollowers(),
                notifyAccountsMessage.friendsVisibilityType(),
                notifyAccountsMessage.visibleToFriendList(),
                notifiedList
        );
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
                .createNativeQuery("UPDATE accounts SET is_verified = 1 WHERE email = :email")
                .setParameter("email",email)
                .executeUpdate();

    }

    @Override
    public List<Long> findNewUsers() {
        long threeMonths = 3*365*24*60*60*1000L;

        return accountRepository.findAll().stream().filter(account->
                new Date().toInstant().getEpochSecond() - account.getCreatedDate().getEpochSecond() <= threeMonths
                        && !account.getUserName().equals("ExampleUser")
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

        return accountRepository.searchAccountsByUserNameOrEmail(searchText);

    }





}
