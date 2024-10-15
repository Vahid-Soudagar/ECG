package com.vcreate.ecg.bluetooth.data;

import java.util.Random;

public class SpO2 {
    public int SPO2_INVALID = 127;
    public int PULSE_RATE_INVALID = 255;

    private int SpO2;
    private int pulseRate;
    private int status;

    public SpO2() {
    }

    public void updateValues() {
        Random rand = new Random();
        this.SpO2 = rand.nextInt(5) + 95; // Random value between 95 and 99
        this.pulseRate = rand.nextInt(40) + 60; // Random value between 60 and 99
        this.status = rand.nextInt(2); // Random value 0 or 1
    }

    public SpO2(int spO2, int pulseRate, int status) {
        SpO2 = spO2;
        this.pulseRate = pulseRate;
        this.status = status;
    }

    public int getSpO2() {
        return SpO2;
    }

    public void setSpO2(int spO2) {
        SpO2 = spO2;
    }

    public int getPulseRate() {
        return pulseRate;
    }


    @Override
    public String toString() {
        return "SpO2: " + (SpO2!=SPO2_INVALID ? SpO2: "- -") +
                "  Pulse Rate:"+(pulseRate!=PULSE_RATE_INVALID ? pulseRate: "- -");
    }
}
