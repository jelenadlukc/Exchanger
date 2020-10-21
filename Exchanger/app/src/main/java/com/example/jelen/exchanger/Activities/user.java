package com.example.jelen.exchanger.Activities;

import java.util.ArrayList;

public class user {
    public String name;
    public String lastName;
    public String email;
    public String password;
    public String number;

    private Double longitude;
    private Double latitude;
    private Integer points;

    public ArrayList<String> friends;
    private Boolean showfriends;
   // private Boolean showplayers;
    private Boolean workback;

    public user()
    {
    }


    public user(String name, String lastName, String email, String password, String number) {
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.number = number;

        this.longitude = 0.0;
        this.latitude = 0.0;
        this.points = 0;

        this.friends = new ArrayList<>();
        friends.add("");

        this.showfriends = true;
        //this.showplayers = true;
        this.workback = true;
    }

    public String getFirstName()
    {
        return name;
    }

    public void setFirstName(String firstName)
    {

        this.name = name;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {

        this.lastName = lastName;
    }



    public Double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    public Integer getPoints(){return this.points;}

    public void setPoints(Integer points) {this.points = points; }

    public ArrayList<String> getFriends()
    {

        return friends;
    }

    public void setFriends(ArrayList<String> friends)
    {
        this.friends = friends;
    }


    public Boolean getShowfriends()
    {

        return showfriends;
    }

    public void setShowfriends(Boolean showfriends)
    {
        this.showfriends = showfriends;
    }

   // public Boolean getShowplayers() { return showplayers; }

    //public void setShowplayers(Boolean showplayers) { this.showplayers = showplayers; }

    public Boolean getWorkback()
    {

        return workback;
    }

    public void setWorkback(Boolean workback)
    {
        this.workback = workback;
    }


}
