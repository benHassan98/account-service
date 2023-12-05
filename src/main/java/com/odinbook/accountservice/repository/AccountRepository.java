package com.odinbook.accountservice.repository;

import com.odinbook.accountservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    public Optional<Account> findByEmail(String email);
    public List<Account> findByUserName(String userName);
    @Query(value = "SELECT * FROM accounts WHERE MATCH (username, email) AGAINST (:searchText)", nativeQuery = true)
    public List<Account> searchAccountsByUserNameOrEmail(@Param("searchText") String searchText);

}
