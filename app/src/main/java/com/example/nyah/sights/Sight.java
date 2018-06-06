package com.example.nyah.sights;

import com.google.android.gms.maps.model.Marker;

public class Sight {

    private String Name;
    private String Description;
    private String ImagePath;
    private String SecondImagePath;
    private String ThirdImagePath;
    private double Lat;
    private double Lon;
    public Marker Marker;
    public int Sity;
    public int Type;

    public int getType() {
        return Type;
    }

    public void setType(int type) {
        Type = type;
    }

    public int getSity() {
        return Sity;
    }

    public void setSity(int sity) {
        Sity = sity;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    //Картинки
    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public String getSecondImagePath() {
        return SecondImagePath;
    }

    public void setSecondImagePath(String secondImagePath) {
        SecondImagePath = secondImagePath;
    }

    public String getThirdImagePath() {
        return ThirdImagePath;
    }

    public void setThirdImagePath(String thirdImagePath) {
        ThirdImagePath = thirdImagePath;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public double getLat() {
        return Lat;
    }

    public void setLat(double lat) {
        Lat = lat;
    }

    public double getLon() {
        return Lon;
    }

    public void setLon(double lon) {
        Lon = lon;
    }
}
