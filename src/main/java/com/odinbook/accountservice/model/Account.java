package com.odinbook.accountservice.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "accounts")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Account {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "picture_id", nullable = false)
  private String pictureId;

  @Column(name = "fullname", nullable = false)
  private String fullName;
  @Column(name = "username", nullable = false)
  private String userName;
  @Column(name = "email", nullable = false, unique = true)
  private String email;
  @JsonIgnore
  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "is_verified")
  private Boolean isVerified = false;
  @Column(name = "about_me")
  private String aboutMe;
  @ManyToMany
  @JoinTable(name = "friends", joinColumns = @JoinColumn(name = "adding_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "added_id", referencedColumnName = "id"))
  private final List<Account> friendList = new ArrayList<>();

  @ManyToMany
  @JoinTable(name = "followers", joinColumns = @JoinColumn(name = "followee_id"), inverseJoinColumns = @JoinColumn(name = "follower_id"))
  private final List<Account> followerList = new ArrayList<>();

  @ManyToMany
  @JoinTable(name = "followers", joinColumns = @JoinColumn(name = "follower_id"), inverseJoinColumns = @JoinColumn(name = "followee_id"))
  private final List<Account> followeeList = new ArrayList<>();

  @JsonIgnore
  @Column(name = "roles", nullable = false)
  private String roles = "ROLE_USER";
  @Column(name = "created_date", nullable = false, updatable = false)
  @CreationTimestamp
  private Timestamp createdDate;

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Account)
      return Objects.equals(this.id, ((Account) obj).getId());

    return false;
  }

  public String getAboutMe() {
    return aboutMe;
  }

  public void setAboutMe(String aboutMe) {
    this.aboutMe = aboutMe;
  }

  public String getRoles() {
    return this.roles;
  }

  public void setRoles(String roles) {
    this.roles = roles;
  }

  public void addRole(String role) {
    setRoles(this.roles + "," + role);
  }

  public Timestamp getCreatedDate() {
    return createdDate;
  }

  public List<Account> getFriendList() {
    return List.copyOf(this.friendList);
  }

  public void addFriend(Account addedAccount) {
    this.friendList.add(addedAccount);
  }

  public void removeFriend(Account account) {
    this.friendList.remove(account);
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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Boolean getVerified() {
    return isVerified;
  }

  public void setVerified(Boolean verified) {
    isVerified = verified;
  }

  public void follow(Account account) {
    this.followeeList.add(account);
  }

  public void unFollow(Account account) {
    this.followeeList.remove(account);
  }

  public List<Account> getFollowerList() {
    return List.copyOf(followerList);
  }

  public List<Account> getFolloweeList() {
    return List.copyOf(followeeList);
  }

  public Boolean getIsVerified() {
    return isVerified;
  }

  public void setIsVerified(Boolean isVerified) {
    this.isVerified = isVerified;
  }

  public void setCreatedDate(Timestamp createdDate) {
    this.createdDate = createdDate;
  }

  public String getPictureId() {
    return pictureId;
  }

  public void setPictureId(String pictureId) {
    this.pictureId = pictureId;
  }

}
