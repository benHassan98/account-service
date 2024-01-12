package com.odinbook.accountservice.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.odinbook.accountservice.model.Account;
import com.odinbook.accountservice.record.AddFriendRecord;
import com.odinbook.accountservice.record.NotifyAccountsRecord;
import org.springframework.messaging.Message;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface AccountService {
    public Account createAccount(Account account);
    public List<Account> findAll();
    public Account findAccountById(Long id) throws NoSuchElementException;
    public Account findAccountByEmail(String email) throws NoSuchElementException;
    public List<Account> findAccountByUserName(String userName);
    public Account updateAccount(Account newAccount) throws NoSuchElementException;
    public void addFriend(String addFriendRecordJson);
    public void removeFriend(Long removingId, Long removedId);
    public List<Long> findNotifiedAccountsToNewPost(NotifyAccountsRecord notifyAccountsRecord);
    public void findNotifiedAccountsFromPost(String notifyAccountsJson);
    public Boolean isEmailUnique(String email);
    public void resetPassword(String newPassword, String email);
    public void verifyAccount(String email);
    public List<Long> findNewUsers();
    public void follow(Long followerId,Long followeeId);
    public void unFollow(Long followerId,Long followeeId);
    public List<Account> searchAccountsByUserNameOrEmail(String searchText);

}
