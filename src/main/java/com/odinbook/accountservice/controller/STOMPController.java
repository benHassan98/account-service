package com.odinbook.accountservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odinbook.accountservice.model.Account;
import com.odinbook.accountservice.record.SearchTextRecord;
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

    @MessageMapping("/accountSearch/{accountId}")
    public void searchAccountsByUserNameOrEmail(@DestinationVariable("accountId") Long accountId,
                                                 @Payload SearchTextRecord searchTextRecord){


        List<Long> accountList = accountService
                .searchAccountsByUserNameOrEmail(searchTextRecord.searchText())
                .stream()
                .map(Account::getId)
                .toList();

        simpMessagingTemplate.convertAndSend(
                "/exchange/accountSearch/"+accountId,
                accountList
                );



    }

}
