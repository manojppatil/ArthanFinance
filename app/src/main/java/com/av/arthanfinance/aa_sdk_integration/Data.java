package com.av.arthanfinance.aa_sdk_integration;

import androidx.annotation.Keep;

@Keep
//public class InitiateConsentResponse {
//
//    private String status;
//
//    private String trackingId;
//
//    private String referenceId;
//
//    private String dataDetails;
//
//    public InitiateConsentResponse(String status, String trackingId, String referenceId, String dataDetails) {
//        this.status = status;
//        this.trackingId = trackingId;
//        this.referenceId = referenceId;
//        this.dataDetails = dataDetails;
//    }
//
//    public InitiateConsentResponse() {
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getTrackingId() {
//        return trackingId;
//    }
//
//    public void setTrackingId(String trackingId) {
//        this.trackingId = trackingId;
//    }
//
//    public String getReferenceId() {
//        return referenceId;
//    }
//
//    public void setReferenceId(String referenceId) {
//        this.referenceId = referenceId;
//    }
//
//    public String getDataDetails() {
//        return dataDetails;
//    }
//
//    public void setDataDetails(String dataDetails) {
//        this.dataDetails = dataDetails;
//    }
//}
public class Data {
    public String status;
    public String consent_handle;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConsent_handle() {
        return consent_handle;
    }

    public void setConsent_handle(String consent_handle) {
        this.consent_handle = consent_handle;
    }
}
