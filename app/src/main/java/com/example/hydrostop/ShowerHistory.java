package com.example.hydrostop;

import com.google.gson.annotations.SerializedName;

public class ShowerHistory {

    private int id;
    private int user;

    @SerializedName("user_name")
    private String userName;

    private int shower;

    @SerializedName("shower_name")
    private String showerName;

    @SerializedName("start_time")
    private String startTime;

    @SerializedName("end_time")
    private String endTime;

    @SerializedName("duration_seconds")
    private int durationSeconds;

    @SerializedName("completed")
    private boolean completed;

    // Getters
    public int getId() { return id; }

    public int getUser() { return user; }

    public String getUserName() { return userName; }

    public int getShower() { return shower; }

    public String getShowerName() { return showerName; }

    public String getStartTime() { return startTime; }

    public String getEndTime() { return endTime; }

    public int getDurationSeconds() { return durationSeconds; }

    public boolean isCompleted() { return completed; }
}


