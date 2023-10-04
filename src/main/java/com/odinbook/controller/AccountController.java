package com.odinbook.controller;

import com.odinbook.model.Account;
import com.odinbook.service.AccountService;
import com.odinbook.validation.AccountForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    public List<Account> findNewUsers(){
        return accountService.findNewUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id){

        return accountService.findAccountById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping("/token")
    public String getPubSubToken(@RequestBody Long accountId){
        return accountService.getClientAccessToken(accountId);
    }

//    @MessageMapping("/sendFriendRequest")
//    public void sendFriendRequest(FriendNotification friendNotification){
//        notificationService.sendFriendRequest(friendNotification);
//    }

//    @MessageMapping("/acceptFriendRequest")
//    public void acceptFriendRequest(FriendNotification friendNotification){
//        notificationService.acceptFriendRequest(friendNotification);
//        accountService.addFriend(friendNotification);
//    }

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

//    TODO
//    With web pubsub

//    @MessageMapping("/account/search")
//    public void searchAccountsByUserNameOrEmail(Map<String,String> params){
//        String searchText = params.get("searchText");
//        String accountId = params.get("accountId");
//        try{
//            List<Account> accountList =  accountService.searchAccountsByUserNameOrEmail(searchText);
//            simpMessagingTemplate.convertAndSend("account/search/"+accountId,accountList);
//        }
//        catch (IOException ioException){
//            throw new RuntimeException(ioException);
//        }
//    }
    @GetMapping("email/{email}")
    public ResponseEntity<?> findByEmail(@PathVariable String email){
        return accountService.findAccountByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.notFound().build());
    }

    @GetMapping("userName/{userName}")
    public List<Account> findByUserName(@PathVariable String userName){
        return accountService.findAccountByUserName(userName);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateAccount(@ModelAttribute Account account){

        return accountService.updateAccount(account)
                .map(ResponseEntity::ok)
                .orElseGet(()->ResponseEntity.badRequest().build());
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyAccount(@RequestBody String email){
        accountService.verifyAccount(email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/resetPassword")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String,String> params){

        String newPassword = params.get("newPassword");
        String email = params.get("email");

        accountService.resetPassword(newPassword,email);

        return ResponseEntity.ok().build();
    }


}
