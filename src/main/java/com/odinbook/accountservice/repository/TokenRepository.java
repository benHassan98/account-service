package com.odinbook.accountservice.repository;

import com.odinbook.accountservice.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Long> {

  public Token findTokenByCode(String code);

}
