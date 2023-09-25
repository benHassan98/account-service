package com.odinbook.service;

import com.odinbook.model.Account;

import java.io.IOException;
import java.util.List;

public interface AccountService {
    public Account createAccount(Account account) throws IOException;
    public List<Account> findAll();
    public Account findAccountById(Long id);
    public Account findAccountByEmail(String email);
    public List<Account> findAccountByUserName(String userName);
    public Account updateAccount(Account account);
    public void addFriend(Long addingId, Long addedId);
    public Boolean isEmailUnique(String email);
    public void resetPassword(String newPassword, String email);
    public void verifyAccount(String email);
    public List<Account> findNewUsers();
    public void follow(Long followerId,Long followeeId);
    public void unFollow(Long followerId,Long followeeId);
    public List<Account> searchAccountsByUserNameOrEmail(String searchText) throws IOException;
}
