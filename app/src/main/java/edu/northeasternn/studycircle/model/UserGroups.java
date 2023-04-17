package edu.northeasternn.studycircle.model;

import java.util.List;

/**
 * A class that maps a user and the various groups they belong to.
 */
public class UserGroups {
    String user;
    List<String> groups;

    public UserGroups() {
    }

    public UserGroups(String user, List<String> groups) {
        this.user = user;
        this.groups = groups;
    }

    public String getUser() {
        return user;
    }

    public List<String> getGroups() {
        return groups;
    }

    @Override
    public String toString() {
        return "UserGroups{" +
                "user='" + user + '\'' +
                ", groups=" + groups.toString() +
                '}';
    }
}