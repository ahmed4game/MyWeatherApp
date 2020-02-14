package com.ahmed.myapplication.models;

public class LocationModel {

    private String areaName;
    private String localName;
    private String stateName;

    public String getAreaName() {
        return areaName;
    }

    @Override
    public String toString() {
        return "LocationModel{" +
                "areaName='" + areaName + '\'' +
                ", localName='" + localName + '\'' +
                ", stateName='" + stateName + '\'' +
                ", lastUpdatedAt='" + lastUpdatedAt + '\'' +
                '}';
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public LocationModel(String areaName, String localName, String stateName, String lastUpdatedAt) {
        this.areaName = areaName;
        this.localName = localName;
        this.stateName = stateName;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(String lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    private String lastUpdatedAt;
}
