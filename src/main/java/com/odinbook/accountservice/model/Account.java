package com.odinbook.accountservice.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.odinbook.accountservice.DTO.ImageDTO;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "accounts")
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "picture",nullable = false)
    private String picture;

    @Transient
    private ImageDTO image;
    @Column(name = "fullname",nullable = false)
    private String fullName;
    @Column(name = "username",nullable = false)
    private String userName;
    @Column(name = "email",nullable = false,unique = true)
    private String email;
    @JsonIgnore
    @Column(name = "password",nullable = false)
    private String password;

    @Column(name = "is_verified")
    private Boolean isVerified = false;
    @Column(name = "about_me")
    private String aboutMe;
    @ManyToMany
    @JoinTable(
            name = "friends",
            joinColumns = @JoinColumn(name = "adding_id",referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "added_id",referencedColumnName = "id")
    )
    private final List<Account> friendList = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "followers",
            joinColumns = @JoinColumn(name = "followee_id"),
            inverseJoinColumns = @JoinColumn(name = "follower_id")
    )
    private final List<Account> followerList = new ArrayList<>();


    @ManyToMany
    @JoinTable(
            name = "followers",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "followee_id")
    )
    private final List<Account> followeeList = new ArrayList<>();

    @JsonIgnore
    @Column(name = "roles" , nullable = false)
    private String roles = "ROLE_USER";
    @Column(name = "created_date", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdDate;

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Account)
            return Objects.equals(this.id, ((Account) obj).getId());

        return false;
    }

    public String getAboutMe() {
        return aboutMe;
    }

    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public String getRoles(){
        return this.roles;
    }

    public void setRoles(String roles){
        this.roles = roles;
    }

    public void addRole(String role){
        setRoles(this.roles+","+role);
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public List<Account> getFriendList() {
        return List.copyOf(this.friendList);
    }

    public void addFriend(Account addedAccount){
        this.friendList.add(addedAccount);
    }

    public void removeFriend(Account account){
        this.friendList.remove(account);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
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

    public void follow(Account account){
        this.followeeList.add(account);
    }
    public void unFollow(Account account){
        this.followeeList.remove(account);
    }

    public List<Account> getFollowerList() {
        return List.copyOf(followerList);
    }

    public List<Account> getFolloweeList() {
        return List.copyOf(followeeList);
    }

    public ImageDTO getImage() {
        return image;
    }

    public void setImage(ImageDTO image) {
        this.image = image;
    }
}
