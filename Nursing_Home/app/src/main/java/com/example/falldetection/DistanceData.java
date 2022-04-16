package com.example.falldetection;

public class DistanceData {
    private float distance;
    private String class_;
    private int count_;
    public DistanceData(float distance,String class_, int count_) {
        this.distance = distance;
        this.class_ = class_;
        this.count_ = count_;
    }
    public float getDistance() {
        return this.distance;
    }
    public String getClass_() {
        return this.class_;
    }
    public int getCount_() {
        return this.count_;
    }
    public void setDistance(float distance) {
        this.distance = distance;
    }
    public void setClass_(String class_) {
        this.class_ = class_;
    }
    public void setCount_(int count) {
        this.count_ = count;
    }
}

