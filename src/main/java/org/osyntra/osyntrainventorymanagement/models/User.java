package org.osyntra.osyntrainventorymanagement.models;

import java.time.Instant;

public abstract class User {
    private Integer id;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private UserStatus status;
    private Instant createdAt;
    private Instant modifiedAt;

    //CONSTRUCTORS
    public User(){}

    public User(
            String firstName,
            String lastName,
            String email,
            String passwordHash
    ){
        this.id=-1;
        this.firstName=firstName;
        this.lastName=lastName;
        this.email=email;
        this.passwordHash=passwordHash;
        this.status=UserStatus.ACTIVE;
        this.createdAt=Instant.now();
        this.modifiedAt=Instant.now();
    }

    public User(User u){
        this.id=u.id;
        this.firstName=u.firstName;
        this.lastName=u.lastName;
        this.email=u.email;
        this.passwordHash=u.passwordHash;
        this.status=u.status;
        this.createdAt=u.createdAt;
        this.modifiedAt=u.modifiedAt;
    }

    //ACCESSORS
    public Integer getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }


    //MUTATORS
    public void setId(Integer id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }
}
