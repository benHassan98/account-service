package com.odinbook.accountservice.service;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.odinbook.accountservice.model.Account;
import com.odinbook.accountservice.record.AddFriendRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface AccountService {
    public Account createAccount(Account account);
    public List<Account> findAll();
    public Optional<Account> findAccountById(Long id);
    public Optional<Account> findAccountByEmail(String email);
    public List<Account> findAccountByUserName(String userName);
    public Optional<Account> updateAccount(Account newAccount);
    public void addFriend(AddFriendRecord addFriendRecord);
    public Boolean isEmailUnique(String email);
    public void resetPassword(String newPassword, String email);
    public void verifyAccount(String email);
    public List<Account> findNewUsers();
    public void follow(Long followerId,Long followeeId);
    public void unFollow(Long followerId,Long followeeId);
    public List<Account> searchAccountsByUserNameOrEmail(String searchText);
    public String getClientAccessToken(Long accountId);

}
