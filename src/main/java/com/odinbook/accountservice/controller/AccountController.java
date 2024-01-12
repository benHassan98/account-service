package com.odinbook.accountservice.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.util.JSONArrayUtils;
import com.nimbusds.jose.util.JSONObjectUtils;
import com.nimbusds.jose.util.JSONStringUtils;
import com.odinbook.accountservice.record.AddFriendRecord;
import com.odinbook.accountservice.record.NotifyAccountsRecord;
import com.odinbook.accountservice.record.TokenRecord;
import com.odinbook.accountservice.service.AccountService;
import com.odinbook.accountservice.validation.AccountForm;
import com.odinbook.accountservice.model.Account;
import jakarta.validation.Valid;
import org.apache.tomcat.util.json.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

@RestController
public class AccountController {
    private final AccountService accountService;
    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }
    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@Valid @ModelAttribute AccountForm accountForm,
                                           BindingResult bindingResult){

        if(bindingResult.hasErrors()){
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return  ResponseEntity.ok(accountService.createAccount(accountForm.getAccount()));

    }

    @GetMapping("/all")
    public List<Account> findAll(){
        return accountService.findAll();
    }
    @GetMapping("/newUsers")
    public List<Long> findNewUsers(){
        return accountService.findNewUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) throws NoSuchElementException {

        return ResponseEntity.ok(accountService.findAccountById(id));
    }

    @PostMapping("/unFriend")
    public void unFriend(@RequestBody Map<String,Long> params){
        Long removingId = params.get("removingId");
        Long removedId = params.get("removedId");
        accountService.removeFriend(removingId, removedId);

    }

    @PostMapping("/follow")
    public void follow(@RequestBody Map<String,Long> params){
        Long followerId = params.get("followerId");
        Long followeeId = params.get("followeeId");
        accountService.follow(followerId,followeeId);

    }

    @PostMapping("/unFollow")
    public void unFollow(@RequestBody Map<String,Long> params){
        Long followerId = params.get("followerId");
        Long followeeId = params.get("followeeId");
        accountService.unFollow(followerId,followeeId);

    }

    @GetMapping("email/{email}")
    public ResponseEntity<?> findByEmail(@PathVariable String email) throws NoSuchElementException {
        return ResponseEntity.ok(accountService.findAccountByEmail(email));

    }

    @GetMapping("userName/{userName}")
    public List<Account> findByUserName(@PathVariable String userName){
        return accountService.findAccountByUserName(userName);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateAccount(@ModelAttribute Account account) throws NoSuchElementException {

        return ResponseEntity.ok(accountService.updateAccount(account));
    }
    @PostMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestBody TokenRecord tokenRecord){
        accountService.verifyAccount(tokenRecord.email());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String,String> params){

        String newPassword = params.get("newPassword");
        String email = params.get("email");

        accountService.resetPassword(newPassword,email);

        return ResponseEntity.ok().build();
    }

    @ExceptionHandler(value = NoSuchElementException.class)
    public ResponseEntity<?> noSuchElementExceptionHandler(){
        return ResponseEntity.notFound().build();
    }
    @ExceptionHandler(value = JsonProcessingException.class)
    public ResponseEntity<?> jsonProcessingExceptionHandler(){
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY.value()).build();
    }
}
