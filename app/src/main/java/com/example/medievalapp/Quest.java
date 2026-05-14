package com.example.medievalapp;

public class Quest {
    public int id;
    public String title;
    public long endTime;
    public int status;

    public Quest(int id, String title, long endTime, int status) {
        this.id = id;
        this.title = title;
        this.endTime = endTime;
        this.status = status;
    }
}
