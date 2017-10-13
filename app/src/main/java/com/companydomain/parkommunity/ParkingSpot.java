package com.companydomain.parkommunity;

/**
 * Created by ori on 26/08/16.
 */

public class ParkingSpot {

    private String id;
    private String longitude;
    private String latitude;
    private String address;
    private String whoGeneratedMe;



    private String whoGeneratedMePhoto;

    public ParkingSpot() {
        // Default constructor required for calls to DataSnapshot.getValue(ParkingSpot.class)
    }

    public ParkingSpot(String longitude, String latitude, String address, String whoGeneratedMe, String whoGeneratedMePhoto) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.whoGeneratedMe = whoGeneratedMe;
        this.whoGeneratedMePhoto = whoGeneratedMePhoto;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getWhoGeneratedMe() {
        return whoGeneratedMe;
    }

    public void setWhoGeneratedMe(String whoGeneratedMe) {
        this.whoGeneratedMe = whoGeneratedMe;
    }

    public String getWhoGeneratedMePhoto() {
        return whoGeneratedMePhoto;
    }

    public void setWhoGeneratedMePhoto(String whoGeneratedMePhoto) {
        this.whoGeneratedMePhoto = whoGeneratedMePhoto;
    }


}
