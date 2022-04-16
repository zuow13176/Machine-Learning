package com.example.falldetection;

public class DataBean {
    private float x;
    private float y;
    private float z;
    private String class_;

    public DataBean(float x, float y, float z, String class_) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.class_ = class_;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    public String getClass_() {
        return this.class_;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setClass_(String class_) {
        this.class_ = class_;
    }
}
