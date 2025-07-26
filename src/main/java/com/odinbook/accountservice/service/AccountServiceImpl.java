package com.odinbook.accountservice.service;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.odinbook.accountservice.model.Account;
import com.odinbook.accountservice.record.AddFollowerRecord;
import com.odinbook.accountservice.repository.AccountRepository;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Service
public class AccountServiceImpl implements AccountService {

  @PersistenceContext
  private EntityManager entityManager;
  private final AccountRepository accountRepository;
  private final PasswordEncoder passwordEncoder;
  private final StringRedisTemplate stringRedisTemplate;
  private final MinioClient minioClient;

  @Autowired
  public AccountServiceImpl(AccountRepository accountRepository,
      PasswordEncoder passwordEncoder, StringRedisTemplate stringRedisTemplate,
      MinioClient minioClient) {
    this.accountRepository = accountRepository;
    this.passwordEncoder = passwordEncoder;
    this.stringRedisTemplate = stringRedisTemplate;
    this.minioClient = minioClient;
  }

  @Override
  public void create(Account account, MultipartFile picture) throws Exception {

    account.setPictureId(passwordEncoder.encode(account.getEmail() + "-" + new Date().toString()));
    account.setPassword(passwordEncoder.encode(account.getPassword()));

    this.minioClient.putObject(PutObjectArgs.builder()
        .bucket("pictures")
        .object(account.getPictureId())
        .stream(picture.getInputStream(), picture.getSize(), -1)
        .build());

    accountRepository.saveAndFlush(account);
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
  public void addFriend(Long addingId, Long addedId) {

    entityManager
        .createNativeQuery("INSERT INTO friends VALUES(:addingId, :addedId)")
        .setParameter("addingId", addingId)
        .setParameter("addedId", addedId)
        .executeUpdate();
  }

  @Override
  @Transactional
  public void removeFriend(Long removingId, Long removedId) {
    entityManager
        .createNativeQuery(
            "DELETE FROM friends WHERE (adding_id = :removingId AND added_id = :removedId) OR (adding_id = :removedId AND added_id = :removingId)")
        .setParameter("removingId", removingId)
        .setParameter("removedId", removedId)
        .executeUpdate();
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
    boolean isFollowBack = entityManager
        .createNativeQuery("SELECT * FROM followers WHERE follower_id = :followeeId AND followee_id = :followerId")
        .setParameter("followerId", followerId)
        .setParameter("followeeId", followeeId)
        .getResultList().size() > 0;

    entityManager
        .createNativeQuery("INSERT INTO followers VALUES(:followerId,:followeeId)")
        .setParameter("followerId", followerId)
        .setParameter("followeeId", followeeId)
        .executeUpdate();

    if (isFollowBack) {
      this.addFriend(followerId, followeeId);
    }
    this.stringRedisTemplate.convertAndSend("addFollowerChannel",
        new AddFollowerRecord(followerId, followeeId, isFollowBack));
  }

  @Override
  @Transactional
  public void unFollow(Long followerId, Long followeeId) {
    boolean areFriends = entityManager
        .createNativeQuery(
            "SELECT * FROM friends WHERE (adding_id = :followerId AND added_id = :followeeId) OR (adding_id = :followeeId AND added_id = :followerId)")
        .setParameter("followerId", followerId)
        .setParameter("followeeId", followeeId)
        .getResultList().size() > 0;

    entityManager
        .createNativeQuery("DELETE FROM followers WHERE follower_id = :followerId AND followee_id = :followeeId")
        .setParameter("followerId", followerId)
        .setParameter("followeeId", followeeId)
        .executeUpdate();

    if (areFriends) {
      this.removeFriend(followeeId, followerId);
    }
  }

  @Override
  public List<Account> searchByUserNameOrEmail(String searchText) {

    return accountRepository.searchAccountsByUserNameOrEmail("%" + searchText + "%");

  }

}
