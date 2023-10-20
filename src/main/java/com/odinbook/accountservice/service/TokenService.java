package com.odinbook.accountservice.service;

import com.odinbook.accountservice.model.Token;
import jakarta.mail.MessagingException;

public interface TokenService {

    public Token createToken(Token token) throws MessagingException;
    public Token verifyToken(String code);
}
