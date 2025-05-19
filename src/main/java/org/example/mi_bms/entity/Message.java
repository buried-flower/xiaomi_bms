package org.example.mi_bms.entity;

import lombok.Getter;

public class Message {
    private Integer signalId; // 数据库中的信号ID
    private int carId;
    private int warnId;
    private String signal;

    public Integer getSignalId() {
        return signalId;
    }

    public void setSignalId(Integer signalId) {
        this.signalId = signalId;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public int getWarnId() {
        return warnId;
    }

    public void setWarnId(int warnId) {
        this.warnId = warnId;
    }

    public String getSignal() {
        return signal;
    }

    public void setSignal(String signal) {
        this.signal = signal;
    }
}
