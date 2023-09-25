package com.odinbook.repository;

import com.odinbook.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    public Optional<Account> findByEmail(String email);
    public List<Account> findByUserName(String userName);
    @Query(value = "UPDATE accounts SET password = :newPassword WHERE email = :email",nativeQuery = true)
    public void resetPassword(@Param("newPassword") String newPassword,
                              @Param("email")String email);

    @Query(value = "UPDATE accounts SET is_verified = 1 WHERE email = :email",nativeQuery = true)
    public void verifyAccount(@Param("email") String email);

    @Query(value = "INSERT INTO followers VALUES(:followerId,:followeeId) ",nativeQuery = true)
    public void follow(@Param("followerId") Long followerId,@Param("followeeId") Long followeeId);

    @Query(value = "DELETE FROM followers WHERE follower_id = :followerId AND followee_id = :followeeId"
            ,nativeQuery = true)
    public void unFollow(@Param("followerId") Long followerId,@Param("followeeId") Long followeeId);

}
