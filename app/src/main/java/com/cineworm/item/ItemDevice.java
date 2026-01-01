package com.cineworm.item;

public class ItemDevice {
    private String userId;
    private String deviceName;
    private String sessionName;
    private boolean isSameUser = false;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getSessionName() {
        return sessionName;
    }

    public void setSessionName(String sessionName) {
        this.sessionName = sessionName;
    }

    public boolean isSameUser() {
        return isSameUser;
    }

    public void setSameUser(boolean sameUser) {
        isSameUser = sameUser;
    }
}
