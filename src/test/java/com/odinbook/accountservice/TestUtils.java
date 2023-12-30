package com.odinbook.accountservice;

import com.odinbook.accountservice.model.Account;
import com.odinbook.accountservice.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class TestUtils {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }

        return salt.toString();
    }

    public Account createRandomAccount(){
        Account account = new Account();
        account.setFullName(getSaltString());
        account.setUserName(getSaltString());
        account.setEmail(getSaltString()+"@gmail.com");
        account.setPassword(passwordEncoder.encode("password"));
        account.setAboutMe(getSaltString());
        account.setPicture("default");


        return accountRepository.saveAndFlush(account);

    }





}
