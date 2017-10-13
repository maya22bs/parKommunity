package com.companydomain.parkommunity;

import java.util.List;

/**
 * Created by ori on 27/08/16.
 */

public class User {

    private String id;
    private String uid;
    private String name;
    private String photoUrl;
    private String email;
    private List<String> myGroups;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(ParkingSpot.class)
    }

    public User(String uid, String name, String photoUrl, String email, List<String> myGroups) {
        this.uid = uid;
        this.name = name;
        this.photoUrl = photoUrl;
        this.email = email;
        this.myGroups = myGroups;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getMyGroups() {
        return myGroups;
    }

    public void setMyGroups(List<String> myGroups) {
        this.myGroups = myGroups;
    }
}
