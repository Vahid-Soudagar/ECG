package com.vcreate.ecg.bluetooth.data;


import java.util.Random;

public class NIBP {
    public int HIGH_PRESSURE_INVALID = 0;
    public int LOW_PRESSURE_INVALID  = 0;

    private int highPressure;
    private int meanPressure;
    private int lowPressure;
    private int coughPressure;
    private int status;

    public NIBP() {
    }

    public void updateValues() {
        Random rand = new Random();
        this.highPressure = rand.nextInt(40) + 110; // Random value between 110 and 149
        this.meanPressure = rand.nextInt(20) + 70; // Random value between 70 and 89
        this.lowPressure = rand.nextInt(30) + 60; // Random value between 60 and 89
        this.coughPressure = rand.nextInt(50); // Random value between 0 and 49
        this.status = rand.nextInt(2); // Random value 0 or 1
    }

    public NIBP(int highPressure, int meanPressure, int lowPressure, int cuffPressure, int status) {
        this.highPressure = highPressure;
        this.meanPressure = meanPressure;
        this.lowPressure = lowPressure;
        this.coughPressure = cuffPressure;
        this.status = status;
    }

    public int getMeanPressure() {
        return meanPressure;
    }

    public int getHighPressure() {
        return highPressure;
    }

    public int getLowPressure() {
        return lowPressure;
    }

    public int getCoughPressure() {
        return coughPressure;
    }

    public int getStatus() {
        return status;
    }

    public void setHighPressure(int highPressure) {
        this.highPressure = highPressure;
    }

    public void setMeanPressure(int meanPressure) {
        this.meanPressure = meanPressure;
    }

    public void setLowPressure(int lowPressure) {
        this.lowPressure = lowPressure;
    }

    public void setCoughPressure(int coughPressure) {
        this.coughPressure = coughPressure;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return  "Cuff:" +  (coughPressure !=0 ? coughPressure : "- -") + "\r\n" +
                "High:"+  (highPressure!=0 ? highPressure: "- -") +
                " Low:" +  (lowPressure !=0 ? lowPressure : "- -") +
                " Mean:"+  (meanPressure!=0 ? meanPressure: "- -");
    }
}
