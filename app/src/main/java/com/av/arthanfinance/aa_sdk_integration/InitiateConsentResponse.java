package com.av.arthanfinance.aa_sdk_integration;

import androidx.annotation.Keep;

@Keep
public class InitiateConsentResponse {
    private String status;
    private String ver;
    public Data data;
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getVer() {
        return ver;
    }
    public void setVer(String ver) {
        this.ver = ver;
    }
    public Data getData() {
        return data;
    }
    public void setData(Data data) {
        this.data = data;
    }
}