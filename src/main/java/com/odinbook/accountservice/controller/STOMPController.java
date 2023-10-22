package com.odinbook.accountservice.controller;

import com.odinbook.accountservice.model.Account;
import com.odinbook.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class STOMPController {

    private final AccountService accountService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public STOMPController(AccountService accountService, SimpMessagingTemplate simpMessagingTemplate) {
        this.accountService = accountService;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("accountSearch/{accountId}")
    public void searchAccountsByUserNameOrEmail(@DestinationVariable("accountId") Long accountId,
                                                 @Payload String searchText){


        List<Account> accountList = accountService
                .searchAccountsByUserNameOrEmail(searchText);

        simpMessagingTemplate.convertAndSend(
                "/exchange/accountSearch/"+accountId,
                accountList
                );



    }

}
