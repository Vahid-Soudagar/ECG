package com.vcreate.ecg.bluetooth.data;


import java.util.Random;

public class ECG {
    public int HEART_RATE_INVALID = 0;
    public int RESP_RATE_INVALID  = 0;

    private int heartRate;
    private int restRate;
    private int status;
    private int stLevel;
    private int arrCode;


    public ECG(int heartRate, int restRate, int status, int stLevel, int arrCode) {
        this.heartRate = heartRate;
        this.restRate = restRate;
        this.status = status;
        this.stLevel = stLevel;
        this.arrCode = arrCode;
    }

    public ECG() {
    }

    public void updateValues() {
        Random rand = new Random();
        this.heartRate = rand.nextInt(40) + 60; // Random value between 60 and 99
        this.restRate = rand.nextInt(10) + 12; // Random value between 12 and 21
        this.status = rand.nextInt(2); // Random value 0 or 1
        this.stLevel = rand.nextInt(5); // Random value between 0 and 4
        this.arrCode = rand.nextInt(100); // Random value between 0 and 99
    }

    public ECG(int heartRate, int restRate, int status) {
        this.heartRate = heartRate;
        this.restRate = restRate;
        this.status = status;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public int getRestRate() {
        return restRate;
    }

    public int getStatus() {
        return status;
    }

    public int getStLevel() {
        return stLevel;
    }

    public void setStLevel(int stLevel) {
        this.stLevel = stLevel;
    }

    public int getArrCode() {
        return arrCode;
    }

    public void setArrCode(int arrCode) {
        this.arrCode = arrCode;
    }

    @Override
    public String toString() {
        return "ECG{" +
                "HEART_RATE_INVALID=" + HEART_RATE_INVALID +
                ", RESP_RATE_INVALID=" + RESP_RATE_INVALID +
                ", heartRate=" + heartRate +
                ", restRate=" + restRate +
                ", status=" + status +
                ", stLevel=" + stLevel +
                ", arrCode=" + arrCode +
                '}';
    }

    public int getHEART_RATE_INVALID() {
        return HEART_RATE_INVALID;
    }

    public void setHEART_RATE_INVALID(int HEART_RATE_INVALID) {
        this.HEART_RATE_INVALID = HEART_RATE_INVALID;
    }

    public int getRESP_RATE_INVALID() {
        return RESP_RATE_INVALID;
    }

    public void setRESP_RATE_INVALID(int RESP_RATE_INVALID) {
        this.RESP_RATE_INVALID = RESP_RATE_INVALID;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public void setRestRate(int restRate) {
        this.restRate = restRate;
    }

    public void setStatus(int status) {
        this.status = status;
    }


}
