package com.odinbook.accountservice.controller;

import com.azure.messaging.webpubsub.WebPubSubServiceClient;
import com.odinbook.accountservice.service.AccountService;
import com.odinbook.accountservice.validation.AccountForm;
import com.odinbook.accountservice.model.Account;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

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
    public ResponseEntity<?> findById(@PathVariable Long id) throws NoSuchElementException {

        return ResponseEntity.ok(accountService.findAccountById(id));
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
    @GetMapping("/clientToken/{accountId}")
    public String getClientToken(@PathVariable Long accountId){
        return accountService.getClientAccessToken(accountId);
    }

    @ExceptionHandler(value = NoSuchElementException.class)
    public ResponseEntity<?> noSuchElementExceptionHandler(){
        return ResponseEntity.notFound().build();
    }

}
