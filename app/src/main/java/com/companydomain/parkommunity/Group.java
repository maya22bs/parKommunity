package com.companydomain.parkommunity;

import java.util.List;

/**
 * Created by ori on 27/08/16.
 */

public class Group {

    private String id;
    private String groupId;
    private String name;
    private List<String> users;
    private String longtitude;
    private String latitude;
    private String whoGeneratedAskForHelpID;
    private String whoGeneratedAskForHelpUserName;
    private String askForHelpGroupName;

    public String getAskForHelpGroupName() {
        return askForHelpGroupName;
    }

    public void setAskForHelpGroupName(String askForHelpGroupName) {
        this.askForHelpGroupName = askForHelpGroupName;
    }



    public String getWhoGeneratedAskForHelpID() {
        return whoGeneratedAskForHelpID;
    }

    public void setWhoGeneratedAskForHelpID(String whoGeneratedAskForHelpID) {
        this.whoGeneratedAskForHelpID = whoGeneratedAskForHelpID;
    }

    public String getWhoGeneratedAskForHelpUserName() {
        return whoGeneratedAskForHelpUserName;
    }

    public void setWhoGeneratedAskForHelpUserName(String whoGeneratedAskForHelpUserName) {
        this.whoGeneratedAskForHelpUserName = whoGeneratedAskForHelpUserName;
    }


    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public String getRadius() {
        return radius;
    }

    public void setRadius(String radius) {
        this.radius = radius;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    private String radius;



    public Group() {
        // Default constructor required for calls to DataSnapshot.getValue(ParkingSpot.class)
    }

    public Group(String groupId, String name, List<String> users, String longtitude, String latitude, String radius) {
        this.groupId = groupId;
        this.name = name;
        this.users = users;
        this.longtitude = longtitude;
        this.latitude = latitude;
        this.radius = radius;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

}
