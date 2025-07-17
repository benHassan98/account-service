package com.odinbook.accountservice.service;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.accountservice.model.Account;
import com.odinbook.accountservice.record.AddFriendRecord;
import com.odinbook.accountservice.repository.AccountRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

  @PersistenceContext
  private EntityManager entityManager;
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository,
      PasswordEncoder passwordEncoder) {
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;

  }

  @Override
  public Account create(Account account) {

    account.setPassword(passwordEncoder.encode(account.getPassword()));

    return accountRepository.saveAndFlush(account);
  }

  @Override
  public List<Account> findAll() {
    return accountRepository.findAll();
  }

  @Override
  public Account findById(Long id) throws NoSuchElementException {
    return accountRepository.findById(id)
        .orElseThrow();
  }

  @Override
  public Account findByEmail(String email) throws NoSuchElementException {
    return accountRepository.findByEmail(email)
        .orElseThrow();
  }

  @Override
  public List<Account> findByUserName(String userName) {
    return accountRepository.findByUserName(userName);
  }

  @Override
  public Account update(Account newAccount) throws NoSuchElementException {

    return accountRepository.findById(newAccount.getId())
        .map(oldAccount -> {
          newAccount.setPassword(oldAccount.getPassword());
          return accountRepository.saveAndFlush(newAccount);
        })
        .orElseThrow();
  }

  @Override
  @Transactional
  public void addFriend(String addFriendRecordJson) {

    AddFriendRecord addFriendRecord;

    try {
      addFriendRecord = new ObjectMapper()
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
          .readValue(addFriendRecordJson, AddFriendRecord.class);
    } catch (JsonProcessingException exception) {
      exception.printStackTrace();
      return;
    }

    Account addingAccount,
        addedAccount;

    try {
      addingAccount = accountRepository.findById(addFriendRecord.addingId())
          .orElseThrow();
      addedAccount = accountRepository.findById(addFriendRecord.addedId())
          .orElseThrow();
    } catch (NoSuchElementException exception) {
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
  public void removeFriend(Long removingId, Long removedId) {

    Account removingAccount,
        removedAccount;

    try {
      removingAccount = accountRepository.findById(removingId)
          .orElseThrow();
      removedAccount = accountRepository.findById(removedId)
          .orElseThrow();
    } catch (NoSuchElementException exception) {
      exception.printStackTrace();
      return;
    }

    removingAccount.removeFriend(removedAccount);
    removedAccount.removeFriend(removingAccount);

    accountRepository.saveAndFlush(removingAccount);
    accountRepository.saveAndFlush(removedAccount);

  }

  @Override
  public Boolean isEmailUnique(String email) {
    return accountRepository.findByEmail(email).isEmpty();
  }

  @Override
  @Transactional
  public void resetPassword(String newPassword, String email) {
    String encodedNewPassword = passwordEncoder.encode(newPassword);

    entityManager
        .createNativeQuery("UPDATE accounts SET password = :newPassword WHERE email = :email")
        .setParameter("newPassword", encodedNewPassword)
        .setParameter("email", email)
        .executeUpdate();

  }

  @Override
  @Transactional
  public void verify(String email) {

    entityManager
        .createNativeQuery("UPDATE accounts SET is_verified = true WHERE email = :email")
        .setParameter("email", email)
        .executeUpdate();

  }

  @Override
  public List<Long> findNewUsers() {
    long threeMonths = 3 * 365 * 24 * 60 * 60 * 1000L;

    return accountRepository.findAll().stream().filter(account -> !"example@gmail.com".equals(account.getEmail()) &&
        new Date().toInstant().getEpochSecond() - account.getCreatedDate().getTime() <= threeMonths).map(Account::getId)
        .toList();

  }

  @Override
  @Transactional
  public void follow(Long followerId, Long followeeId) {

    entityManager
        .createNativeQuery("INSERT INTO followers VALUES(:followerId,:followeeId)")
        .setParameter("followerId", followerId)
        .setParameter("followeeId", followeeId)
        .executeUpdate();

  }

  @Override
  @Transactional
  public void unFollow(Long followerId, Long followeeId) {
    entityManager
        .createNativeQuery("DELETE FROM followers WHERE follower_id = :followerId AND followee_id = :followeeId")
        .setParameter("followerId", followerId)
        .setParameter("followeeId", followeeId)
        .executeUpdate();
  }

  @Override
  public List<Account> searchByUserNameOrEmail(String searchText) {

    return accountRepository.searchAccountsByUserNameOrEmail("%" + searchText + "%");

  }

}
