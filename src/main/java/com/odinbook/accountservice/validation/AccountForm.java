package com.odinbook.accountservice.validation;

import com.odinbook.accountservice.model.Account;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class AccountForm {

  private Long id;
  @NotEmpty
  @Size(min = 3, max = 40)
  private String fullName;
  @NotEmpty
  @Size(min = 3, max = 40)
  private String userName;
  @NotEmpty
  @Email
  @UniqueEmail
  private String email;
  @NotEmpty
  @Size(min = 6, max = 40)
  private String password;
  @NotEmpty
  @Size(min = 6, max = 40)
  private String passwordConfirm;
  private String aboutMe;

  public Account getAccount() {
    Account account = new Account();

    account.setId(this.id);
    account.setFullName(this.fullName);
    account.setUserName(this.userName);
    account.setEmail(this.email);
    account.setPassword(this.password);
    account.setAboutMe(this.aboutMe);

    return account;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAboutMe() {
    return aboutMe;
  }

  public void setAboutMe(String aboutMe) {
    this.aboutMe = aboutMe;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getPasswordConfirm() {
    return passwordConfirm;
  }

  public void setPasswordConfirm(String passwordConfirm) {
    this.passwordConfirm = passwordConfirm;
  }
}
