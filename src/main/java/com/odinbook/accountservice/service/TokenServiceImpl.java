package com.odinbook.accountservice.service;

import com.odinbook.accountservice.model.Token;
import com.odinbook.accountservice.repository.TokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TokenServiceImpl implements TokenService{
    private final TokenRepository tokenRepository;
    private final JavaMailSender javaMailSender;
    @Value("${app.url}")
    private String appUrl;
    private final List<String> tokenTypeList = new ArrayList<>(List.of("verifyAccount","resetPassword"));

    @Autowired
    public TokenServiceImpl(TokenRepository tokenRepository,
                            JavaMailSender javaMailSender) {
        this.tokenRepository = tokenRepository;
        this.javaMailSender = javaMailSender;
    }

    @Override
    public Token createToken(Token token) throws MessagingException {
        token.setCode(UUID.randomUUID().toString());

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);

        if(!tokenTypeList.contains(token.getType())){
            return null;
        }

        String message = token.getType().equals("verifyAccount")?createVerifyAccountMessage(token):
                createResetPasswordMessage(token);

        mimeMessageHelper.setTo(token.getAccountEmail());
        mimeMessageHelper.setSubject(token.getType());
        mimeMessageHelper.setText(message);

        javaMailSender.send(mimeMessage);

        return tokenRepository.saveAndFlush(token);
    }

    @Override
    public Token verifyToken(String code) {
        long twentyMins = 20*60*1000L;
        Token token = tokenRepository.findTokenByCode(code);
        if(
                Objects.isNull(token) ||
                new Date().toInstant().getEpochSecond()-token.getCreatedDate().getTime() > twentyMins
        ){
            return null;
        }
        tokenRepository.deleteById(token.getId());

        return token;
    }
    public String createVerifyAccountMessage(Token token){

        String confirmationUrl = appUrl + "/redirect/"+token.getType()+"?token=" + token.getCode();

        return "Hello,this Abdullah from OdinBook"+"\n\n"
                +"Please follow the link to verify your account:"+"\n\n"
                +confirmationUrl+"\n\n"
                +"the above link will expire in 20 minutes.";
    }

    public String createResetPasswordMessage(Token token){

        String confirmationUrl = appUrl + "/redirect/"+token.getType()+"?token=" + token.getCode();

        return "Hello,this Abdullah from OdinBook"+"\n\n"
                +"Please follow the link to reset your password:"+"\n\n"
                +confirmationUrl+"\n\n"
                +"the above link will expire in 20 minutes.";
    }
}
