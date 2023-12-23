package com.av.arthanfinance.aa_sdk_integration;

import androidx.annotation.Keep;

public class InitiateConsentRequest {
    private final String vua;

    private final String partyIdentifierType;

    private final String partyIdentifierValue;

    private final String productID;

    private final String accountID;

//    private final String templateType = "UNDERWRITING";

//    private final String trackingId = UUID.randomUUID().toString();

//    private final int numberOfMonths = 9;

    public String getVua() {
        return vua;
    }

    public String getPartyIdentifierType() {
        return partyIdentifierType;
    }

    public String getPartyIdentifierValue() {
        return partyIdentifierValue;
    }

    public String getProductID() {
        return productID;
    }

    public String getAccountID() {
        return accountID;
    }

//    public String getTemplateType() {
//        return templateType;
//    }

//    public String getTrackingId() {
//        return trackingId;
//    }

//    public int getNumberOfMonths() {
//        return numberOfMonths;
//    }

    @Keep
    public InitiateConsentRequest(String vuaId, String partyIdentifierType, String partyIdentifierValue, String productID, String accountID) {
        this.vua = vuaId;
        this.partyIdentifierType = partyIdentifierType;
        this.partyIdentifierValue = partyIdentifierValue;
        this.productID = productID;
        this.accountID = accountID;
    }

}
