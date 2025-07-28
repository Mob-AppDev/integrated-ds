package com.devsync.dto;

import jakarta.validation.constraints.NotBlank;

public class FCMTokenDto {
    @NotBlank
    private String token;
    
    private String deviceType; // "android", "ios", "web"
    private String deviceId;

    // Constructors
    public FCMTokenDto() {}

    public FCMTokenDto(String token, String deviceType, String deviceId) {
        this.token = token;
        this.deviceType = deviceType;
        this.deviceId = deviceId;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}