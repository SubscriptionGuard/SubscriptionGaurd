package com.tfmhntspn.subguard.models;

public class SharedBill {

    private String billId;
    private String subscriptionId;
    private String ownerId;
    private String groupId;
    private long totalCents;
    private String payerId;
    private String status;

    public SharedBill() {}

    public SharedBill( String billId, String subscriptionId,
                       String ownerId, String groupId,
                       long totalCents, String payerId, String status){
        this.billId = billId;
        this.subscriptionId = subscriptionId;
        this.ownerId = ownerId;
        this.groupId = groupId;
        this.totalCents = totalCents;
        this.payerId = payerId;
        this.status = status;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public long getTotalCents() {
        return totalCents;
    }

    public void setTotalCents(long totalCents) {
        this.totalCents = totalCents;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
