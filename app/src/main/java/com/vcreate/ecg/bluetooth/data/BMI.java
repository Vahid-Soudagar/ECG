package com.vcreate.ecg.bluetooth.data;

import java.util.Random;

public class BMI {
    private int diastolic;
    private int systolic;
    private int cuff;

    public BMI() {
    }

    public BMI(int diastolic, int systolic, int cuff) {
        this.diastolic = diastolic;
        this.systolic = systolic;
        this.cuff = cuff;
    }

    public void updateValues() {
        Random rand = new Random();
        this.diastolic = rand.nextInt(80) + 60; // Random value between 60 and 139
        this.systolic = rand.nextInt(60) + 90; // Random value between 90 and 149
        this.cuff = rand.nextInt(10) + 1; // Random value between 1 and 10
    }

    public int getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(int diastolic) {
        this.diastolic = diastolic;
    }

    public int getSystolic() {
        return systolic;
    }

    public void setSystolic(int systolic) {
        this.systolic = systolic;
    }

    public int getCuff() {
        return cuff;
    }

    public void setCuff(int cuff) {
        this.cuff = cuff;
    }

    @Override
    public String toString() {
        return "BMI{" +
                "diastolic=" + diastolic +
                ", systolic=" + systolic +
                ", cuff=" + cuff +
                '}';
    }
}
