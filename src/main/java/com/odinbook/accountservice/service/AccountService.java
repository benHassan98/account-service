package com.odinbook.accountservice.service;

import java.util.List;
import java.util.NoSuchElementException;

import com.odinbook.accountservice.model.Account;

public interface AccountService {
  public Account create(Account account);

  public List<Account> findAll();

  public Account findById(Long id) throws NoSuchElementException;

  public Account findByEmail(String email) throws NoSuchElementException;

  public List<Account> findByUserName(String userName);

  public Account update(Account newAccount) throws NoSuchElementException;

  public void addFriend(String addFriendRecordJson);

  public void removeFriend(Long removingId, Long removedId);

  public Boolean isEmailUnique(String email);

  public void resetPassword(String newPassword, String email);

  public void verify(String email);

  public List<Long> findNewUsers();

  public void follow(Long followerId, Long followeeId);

  public void unFollow(Long followerId, Long followeeId);

  public List<Account> searchByUserNameOrEmail(String searchText);

}
