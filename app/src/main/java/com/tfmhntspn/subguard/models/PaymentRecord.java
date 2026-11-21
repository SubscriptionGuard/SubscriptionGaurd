package com.tfmhntspn.subguard.models;

public class PaymentRecord {

    private String paymentId;
    private String shareId;
    private String billingDate;
    private long amountCents;
    private String status;
    private long paidAt;

    public PaymentRecord() {}

    public PaymentRecord(String paymentId, String shareId,
                         String billingDate, long amountCents,
                         String status, long paidAt) {
        this.paymentId = paymentId;
        this.shareId = shareId;
        this.billingDate = billingDate;
        this.amountCents = amountCents;
        this.status = status;
        this.paidAt = paidAt;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getShareId() {
        return shareId;
    }

    public void setShareId(String shareId) {
        this.shareId = shareId;
    }

    public String getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(String billingDate) {
        this.billingDate = billingDate;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(long amountCents) {
        this.amountCents = amountCents;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(long paidAt) {
        this.paidAt = paidAt;
    }

}
