package com.odinbook.accountservice.controller;

import com.odinbook.accountservice.model.Token;
import com.odinbook.accountservice.record.TokenRecord;
import com.odinbook.accountservice.service.TokenService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/token")
public class TokenController {
    private final TokenService tokenService;

    @Autowired
    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createToken(@RequestBody Token token){

        try{
            Token createdToken = tokenService.createToken(token);
            return Objects.isNull(createdToken)?
                    ResponseEntity.badRequest().build():ResponseEntity.ok().build();
        }
        catch (MessagingException exception){

            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
        }

    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestBody TokenRecord tokenRecord){

        Token token = tokenService.verifyToken(tokenRecord.code());
        return Objects.isNull(token)?
                ResponseEntity.badRequest().build():ResponseEntity.ok(token);
    }
}
