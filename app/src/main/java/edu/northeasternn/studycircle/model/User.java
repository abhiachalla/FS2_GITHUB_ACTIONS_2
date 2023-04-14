package edu.northeasternn.studycircle.model;

public class User {
    private String firstName;
    private String lastName;
    private String userName;
    private String emailAddress;
    private String location;
    private String joinedDate;

    public User(String firstName, String lastName, String userName, String emailAddress, String location, String joinedDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.location = location;
        this.emailAddress = emailAddress;
        this.joinedDate = joinedDate;
    }

    public User() {

    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getLocation() { return location; }

    public String getJoinedDate() {
        return joinedDate;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

}

