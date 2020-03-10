package com.example.ta_firebaseauth;

// A class for getting data from Firebase Database
// Used in HistoryActivity
public class PointValue {
    private Long time;
    private Integer temp;
    private Integer hum;
    private Integer moist;
    private Long light;

    public PointValue() {
    }

    public PointValue(Long time, Integer temp, Integer hum, Integer moist, Long light) {
        this.time = time;
        this.temp = temp;
        this.hum = hum;
        this.moist = moist;
        this.light = light;
    }

    public Long getTime() {
        return time;
    }

    public Integer getTemp() {
        return temp;
    }

    public Integer getHum() {
        return hum;
    }

    public Integer getMoist() {
        return moist;
    }

    public Long getLight() {
        return light;
    }
}
