package com.odinbook.validation;

import com.odinbook.service.AccountService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail,String> {
    @Autowired
    private AccountService accountService;
    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        return accountService.isEmailUnique(email);
    }
}
