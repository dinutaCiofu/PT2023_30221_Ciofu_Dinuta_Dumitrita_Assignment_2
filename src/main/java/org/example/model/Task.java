package org.example.model;

public class Task {
    private Integer ID;
    private Integer arrivalTime;
    private Integer serviceTime;
    private Integer totalWaitingTime;


    public Task(Integer ID, Integer arrivalTime, Integer serviceTime) {
        this.ID = ID;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
        totalWaitingTime = 0;
    }

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Integer getArrivalTime() {
        return arrivalTime;
    }

    public Integer getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(Integer serviceTime) {
        this.serviceTime = serviceTime;
    }

    public String toString() {
        return "(" + this.ID + "," + this.arrivalTime + "," + this.serviceTime + ") ";
    }
}
