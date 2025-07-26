package com.odinbook.accountservice.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.odinbook.accountservice.model.Account;
import com.odinbook.accountservice.record.TokenRecord;
import com.odinbook.accountservice.service.AccountService;
import com.odinbook.accountservice.validation.AccountForm;

import jakarta.validation.Valid;

@RestController
public class AccountController {
  private final AccountService accountService;

  @Autowired
  public AccountController(AccountService accountService) {
    this.accountService = accountService;
  }

  @PostMapping()
  public ResponseEntity<?> createAccount(@Valid @ModelAttribute("account") AccountForm accountForm,
      BindingResult bindingResult, @RequestPart("picture") MultipartFile picture) {

    if (bindingResult.hasErrors()) {
      return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
    }
    try {
      accountService.create(accountForm.getAccount(), picture);
    } catch (Exception exception) {
      exception.printStackTrace();
      ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
    }
    return ResponseEntity.ok().build();

  }

  @GetMapping()
  public List<Account> findAll() {
    return accountService.findAll();
  }

  @GetMapping("/newUsers")
  public List<Long> findNewUsers() {
    return accountService.findNewUsers();
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> findById(@PathVariable Long id) throws NoSuchElementException {

    return ResponseEntity.ok(accountService.findById(id));
  }

  @PostMapping("/follow")
  public void follow(@RequestBody Map<String, Long> params) {
    Long followerId = params.get("followerId");
    Long followeeId = params.get("followeeId");
    accountService.follow(followerId, followeeId);

  }

  @PostMapping("/unFollow")
  public void unFollow(@RequestBody Map<String, Long> params) {
    Long followerId = params.get("followerId");
    Long followeeId = params.get("followeeId");
    accountService.unFollow(followerId, followeeId);

  }

  @GetMapping("/email/{email}")
  public ResponseEntity<?> findByEmail(@PathVariable String email) throws NoSuchElementException {
    return ResponseEntity.ok(accountService.findByEmail(email));

  }

  @GetMapping("/userName/{userName}")
  public List<Account> findByUserName(@PathVariable String userName) {
    return accountService.findByUserName(userName);
  }

  @PutMapping()
  public ResponseEntity<?> updateAccount(@ModelAttribute Account account) throws NoSuchElementException {

    return ResponseEntity.ok(accountService.update(account));
  }

  @PostMapping("/verify")
  public ResponseEntity<?> verifyAccount(@RequestBody TokenRecord tokenRecord) {
    accountService.verify(tokenRecord.email());
    return ResponseEntity.ok().build();
  }

  @PutMapping("/resetPassword")
  public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> params) {

    String newPassword = params.get("newPassword");
    String email = params.get("email");

    accountService.resetPassword(newPassword, email);

    return ResponseEntity.ok().build();
  }

  @ExceptionHandler(value = NoSuchElementException.class)
  public ResponseEntity<?> noSuchElementExceptionHandler() {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(value = JsonProcessingException.class)
  public ResponseEntity<?> jsonProcessingExceptionHandler() {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY.value()).build();
  }

}
